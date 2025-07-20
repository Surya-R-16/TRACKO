package com.expensetracker.data.sms

import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.data.sms.model.SmsMessage
import com.expensetracker.data.sms.util.DuplicateDetectionService
import com.expensetracker.data.sms.util.DuplicateDetectionSummary
import com.expensetracker.data.sms.util.SmsFilter
import com.expensetracker.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParserServiceImpl @Inject constructor(
    private val duplicateDetectionService: DuplicateDetectionService
) : SmsParserService {
    
    companion object {
        // Regex patterns for amount extraction
        private val AMOUNT_PATTERNS = listOf(
            "₹\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)".toRegex(RegexOption.IGNORE_CASE),
            "rs\\.?\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)".toRegex(RegexOption.IGNORE_CASE),
            "inr\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)".toRegex(RegexOption.IGNORE_CASE),
            "rupees\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)".toRegex(RegexOption.IGNORE_CASE)
        )
        
        // Regex patterns for UPI ID extraction
        private val UPI_ID_PATTERNS = listOf(
            "([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+)".toRegex(),
            "to\\s+([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+)".toRegex(RegexOption.IGNORE_CASE),
            "paid\\s+to\\s+([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+)".toRegex(RegexOption.IGNORE_CASE)
        )
        
        // Regex patterns for phone number extraction
        private val PHONE_PATTERNS = listOf(
            "\\b([6-9]\\d{9})\\b".toRegex(),
            "to\\s+(\\d{10})".toRegex(RegexOption.IGNORE_CASE),
            "paid\\s+to\\s+(\\d{10})".toRegex(RegexOption.IGNORE_CASE)
        )
        
        // Regex patterns for merchant name extraction
        private val MERCHANT_PATTERNS = listOf(
            "at\\s+([A-Z][A-Z0-9\\s]{2,20})".toRegex(),
            "to\\s+([A-Z][A-Z0-9\\s]{2,20})(?:\\s+on|\\s+at|\\s*$)".toRegex(),
            "paid\\s+to\\s+([A-Z][A-Z0-9\\s]{2,20})".toRegex(),
            "spent\\s+at\\s+([A-Z][A-Z0-9\\s]{2,20})".toRegex(),
            "purchase\\s+at\\s+([A-Z][A-Z0-9\\s]{2,20})".toRegex()
        )
        
        // Regex patterns for transaction ID extraction
        private val TRANSACTION_ID_PATTERNS = listOf(
            "ref\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
            "reference\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
            "txn\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
            "transaction\\s+id\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
            "upi\\s+ref\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE)
        )
        
        // Payment method keywords
        private val UPI_KEYWORDS = listOf("upi", "bhim", "gpay", "phonepe", "paytm", "whatsapp")
        private val CARD_KEYWORDS = listOf("card", "debit", "credit")
        private val NET_BANKING_KEYWORDS = listOf("net banking", "netbanking", "online")
        private val WALLET_KEYWORDS = listOf("wallet", "prepaid", "paytm wallet", "mobikwik")
    }
    
    override suspend fun parseTransactionSms(smsMessage: SmsMessage): ParsedTransaction? = withContext(Dispatchers.Default) {
        if (!canParseTransaction(smsMessage)) {
            return@withContext null
        }
        
        val smsContent = smsMessage.body
        val sender = smsMessage.address
        
        // Extract transaction details
        val amount = extractAmount(smsContent) ?: return@withContext null
        val recipient = extractRecipient(smsContent)
        val merchantName = extractMerchantName(smsContent)
        val transactionId = extractTransactionId(smsContent)
        val paymentMethod = determinePaymentMethod(smsContent, sender)
        
        // Calculate confidence based on extracted data quality
        val confidence = calculateConfidence(amount, recipient, merchantName, transactionId, paymentMethod)
        
        return@withContext ParsedTransaction(
            amount = amount,
            recipient = recipient,
            merchantName = merchantName,
            transactionId = transactionId,
            paymentMethod = paymentMethod,
            dateTime = smsMessage.date,
            smsContent = smsContent,
            sender = sender,
            confidence = confidence
        )
    }
    
    override suspend fun parseTransactionSmsMessages(smsMessages: List<SmsMessage>): List<ParsedTransaction> = withContext(Dispatchers.Default) {
        val parsedTransactions = mutableListOf<ParsedTransaction>()
        
        for (smsMessage in smsMessages) {
            val parsedTransaction = parseTransactionSms(smsMessage)
            if (parsedTransaction != null && parsedTransaction.isValid()) {
                parsedTransactions.add(parsedTransaction)
            }
        }
        
        // Remove duplicates within the parsed transactions themselves
        return@withContext duplicateDetectionService.removeDuplicates(parsedTransactions)
    }
    
    override suspend fun parseAndFilterDuplicates(
        smsMessages: List<SmsMessage>,
        existingTransactions: List<Transaction>
    ): DuplicateDetectionSummary = withContext(Dispatchers.Default) {
        // First parse all SMS messages
        val parsedTransactions = parseTransactionSmsMessages(smsMessages)
        
        // Then check for duplicates against existing transactions
        return@withContext duplicateDetectionService.getDuplicateDetectionSummary(
            parsedTransactions,
            existingTransactions
        )
    }
    
    override fun canParseTransaction(smsMessage: SmsMessage): Boolean {
        return SmsFilter.isTransactionSms(smsMessage) && extractAmount(smsMessage.body) != null
    }
    
    override fun extractAmount(smsContent: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(smsContent)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                return try {
                    amountStr.toDouble()
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
        return null
    }
    
    override fun extractRecipient(smsContent: String): String? {
        // Try UPI ID patterns first
        for (pattern in UPI_ID_PATTERNS) {
            val match = pattern.find(smsContent)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        
        // Try phone number patterns
        for (pattern in PHONE_PATTERNS) {
            val match = pattern.find(smsContent)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        
        return null
    }
    
    private fun extractMerchantName(smsContent: String): String? {
        for (pattern in MERCHANT_PATTERNS) {
            val match = pattern.find(smsContent)
            if (match != null) {
                val merchantName = match.groupValues[1].trim()
                // Filter out common false positives
                if (isValidMerchantName(merchantName)) {
                    return merchantName
                }
            }
        }
        return null
    }
    
    override fun extractTransactionId(smsContent: String): String? {
        for (pattern in TRANSACTION_ID_PATTERNS) {
            val match = pattern.find(smsContent)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }
    
    override fun determinePaymentMethod(smsContent: String, sender: String): String {
        val content = smsContent.lowercase()
        val senderLower = sender.lowercase()
        
        // Check for UPI keywords
        if (UPI_KEYWORDS.any { content.contains(it) } || 
            listOf("gpay", "phonepe", "paytm", "bhim").any { senderLower.contains(it) }) {
            return ParsedTransaction.METHOD_UPI
        }
        
        // Check for card keywords
        if (CARD_KEYWORDS.any { content.contains(it) }) {
            return if (content.contains("credit")) {
                ParsedTransaction.METHOD_CREDIT_CARD
            } else {
                ParsedTransaction.METHOD_DEBIT_CARD
            }
        }
        
        // Check for net banking keywords
        if (NET_BANKING_KEYWORDS.any { content.contains(it) }) {
            return ParsedTransaction.METHOD_NET_BANKING
        }
        
        // Check for wallet keywords
        if (WALLET_KEYWORDS.any { content.contains(it) }) {
            return ParsedTransaction.METHOD_WALLET
        }
        
        // Default based on sender
        return when {
            senderLower.contains("card") -> ParsedTransaction.METHOD_DEBIT_CARD
            listOf("hdfc", "sbi", "icici", "axis", "kotak").any { senderLower.contains(it) } -> ParsedTransaction.METHOD_DEBIT_CARD
            else -> ParsedTransaction.METHOD_OTHER
        }
    }
    
    private fun isValidMerchantName(merchantName: String): String {
        val name = merchantName.uppercase().trim()
        
        // Filter out common false positives
        val invalidNames = listOf(
            "ACCOUNT", "CARD", "BANK", "PAYMENT", "TRANSACTION", "TRANSFER",
            "DEBIT", "CREDIT", "BALANCE", "AMOUNT", "RUPEES", "INR", "RS"
        )
        
        return if (invalidNames.any { name.contains(it) } || name.length < 3) {
            null
        } else {
            merchantName.trim()
        }
    }
    
    private fun calculateConfidence(
        amount: Double,
        recipient: String?,
        merchantName: String?,
        transactionId: String?,
        paymentMethod: String
    ): Float {
        var confidence = 0.5f // Base confidence
        
        // Amount extracted successfully
        if (amount > 0) confidence += 0.2f
        
        // Has recipient or merchant
        if (!recipient.isNullOrBlank() || !merchantName.isNullOrBlank()) confidence += 0.2f
        
        // Has transaction ID
        if (!transactionId.isNullOrBlank()) confidence += 0.1f
        
        // Payment method determined
        if (paymentMethod != ParsedTransaction.METHOD_OTHER) confidence += 0.1f
        
        // Has both recipient and merchant (very detailed)
        if (!recipient.isNullOrBlank() && !merchantName.isNullOrBlank()) confidence += 0.1f
        
        return confidence.coerceIn(0.0f, 1.0f)
    }
}