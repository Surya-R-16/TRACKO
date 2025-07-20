package com.expensetracker.data.sms.util

import com.expensetracker.data.sms.model.ParsedTransaction
import java.util.Date

/**
 * Utility class for parsing bank-specific SMS formats
 */
object BankSpecificParser {
    
    /**
     * Parses HDFC Bank SMS messages
     */
    fun parseHdfcSms(smsContent: String, dateTime: Date): ParsedTransaction? {
        val patterns = listOf(
            // Debit card transaction
            "Rs\\.(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+debited\\s+from\\s+.*?\\s+at\\s+([A-Z][A-Z0-9\\s]+)\\s+on\\s+([\\d-]+)".toRegex(),
            // UPI transaction
            "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+debited\\s+from\\s+.*?\\s+UPI\\s+Ref\\s*:?\\s*([A-Z0-9]+)".toRegex(),
            // Credit card transaction
            "INR\\s+(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+spent\\s+on\\s+([A-Z][A-Z0-9\\s]+)\\s+using\\s+HDFC\\s+.*?Card".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(smsContent)
            if (match != null) {
                val amount = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: continue
                val merchant = if (match.groupValues.size > 2) match.groupValues[2].trim() else null
                val transactionId = if (match.groupValues.size > 3) match.groupValues[3] else null
                
                return ParsedTransaction(
                    amount = amount,
                    recipient = null,
                    merchantName = merchant,
                    transactionId = transactionId,
                    paymentMethod = determineHdfcPaymentMethod(smsContent),
                    dateTime = dateTime,
                    smsContent = smsContent,
                    sender = "HDFC",
                    confidence = 0.9f
                )
            }
        }
        return null
    }
    
    /**
     * Parses SBI Bank SMS messages
     */
    fun parseSbiSms(smsContent: String, dateTime: Date): ParsedTransaction? {
        val patterns = listOf(
            // Debit transaction
            "Rs\\s+(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+debited\\s+from\\s+.*?\\s+on\\s+([\\d-]+)\\s+at\\s+([A-Z][A-Z0-9\\s]+)".toRegex(),
            // UPI transaction
            "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+sent\\s+via\\s+UPI\\s+to\\s+([^\\s]+)\\s+Ref\\s*:?\\s*([A-Z0-9]+)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(smsContent)
            if (match != null) {
                val amount = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: continue
                val merchantOrRecipient = if (match.groupValues.size > 2) match.groupValues[2].trim() else null
                val transactionId = if (match.groupValues.size > 3) match.groupValues[3] else null
                
                return ParsedTransaction(
                    amount = amount,
                    recipient = if (merchantOrRecipient?.contains("@") == true) merchantOrRecipient else null,
                    merchantName = if (merchantOrRecipient?.contains("@") != true) merchantOrRecipient else null,
                    transactionId = transactionId,
                    paymentMethod = determineSbiPaymentMethod(smsContent),
                    dateTime = dateTime,
                    smsContent = smsContent,
                    sender = "SBI",
                    confidence = 0.9f
                )
            }
        }
        return null
    }
    
    /**
     * Parses ICICI Bank SMS messages
     */
    fun parseIciciSms(smsContent: String, dateTime: Date): ParsedTransaction? {
        val patterns = listOf(
            // Debit card transaction
            "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+debited\\s+from\\s+.*?\\s+at\\s+([A-Z][A-Z0-9\\s]+)\\s+on\\s+([\\d-]+)".toRegex(),
            // UPI transaction
            "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+transferred\\s+to\\s+([^\\s]+)\\s+via\\s+UPI\\s+Ref\\s*:?\\s*([A-Z0-9]+)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(smsContent)
            if (match != null) {
                val amount = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: continue
                val merchantOrRecipient = if (match.groupValues.size > 2) match.groupValues[2].trim() else null
                val transactionId = if (match.groupValues.size > 3) match.groupValues[3] else null
                
                return ParsedTransaction(
                    amount = amount,
                    recipient = if (merchantOrRecipient?.contains("@") == true) merchantOrRecipient else null,
                    merchantName = if (merchantOrRecipient?.contains("@") != true) merchantOrRecipient else null,
                    transactionId = transactionId,
                    paymentMethod = determineIciciPaymentMethod(smsContent),
                    dateTime = dateTime,
                    smsContent = smsContent,
                    sender = "ICICI",
                    confidence = 0.9f
                )
            }
        }
        return null
    }
    
    /**
     * Parses GPay SMS messages
     */
    fun parseGpaySms(smsContent: String, dateTime: Date): ParsedTransaction? {
        val patterns = listOf(
            // Standard GPay payment
            "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+to\\s+([^\\s]+)\\s+via\\s+UPI\\s+UPI\\s+Ref\\s*:?\\s*([A-Z0-9]+)".toRegex(),
            // GPay merchant payment
            "You\\s+paid\\s+₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+to\\s+([^\\s]+)\\s+UPI\\s+Ref\\s*:?\\s*([A-Z0-9]+)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(smsContent)
            if (match != null) {
                val amount = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: continue
                val recipient = if (match.groupValues.size > 2) match.groupValues[2].trim() else null
                val transactionId = if (match.groupValues.size > 3) match.groupValues[3] else null
                
                return ParsedTransaction(
                    amount = amount,
                    recipient = if (recipient?.contains("@") == true) recipient else null,
                    merchantName = if (recipient?.contains("@") != true) recipient else null,
                    transactionId = transactionId,
                    paymentMethod = ParsedTransaction.METHOD_UPI,
                    dateTime = dateTime,
                    smsContent = smsContent,
                    sender = "GPAY",
                    confidence = 0.95f
                )
            }
        }
        return null
    }
    
    /**
     * Parses PhonePe SMS messages
     */
    fun parsePhonePeSms(smsContent: String, dateTime: Date): ParsedTransaction? {
        val patterns = listOf(
            // Standard PhonePe payment
            "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+sent\\s+to\\s+([^\\s]+)\\s+via\\s+PhonePe\\s+UPI\\s+ID\\s*:?\\s*([A-Z0-9]+)".toRegex(),
            // PhonePe merchant payment
            "You\\s+sent\\s+₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+to\\s+([^\\s]+)\\s+Transaction\\s+ID\\s*:?\\s*([A-Z0-9]+)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(smsContent)
            if (match != null) {
                val amount = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: continue
                val recipient = if (match.groupValues.size > 2) match.groupValues[2].trim() else null
                val transactionId = if (match.groupValues.size > 3) match.groupValues[3] else null
                
                return ParsedTransaction(
                    amount = amount,
                    recipient = if (recipient?.contains("@") == true) recipient else null,
                    merchantName = if (recipient?.contains("@") != true) recipient else null,
                    transactionId = transactionId,
                    paymentMethod = ParsedTransaction.METHOD_UPI,
                    dateTime = dateTime,
                    smsContent = smsContent,
                    sender = "PHONEPE",
                    confidence = 0.95f
                )
            }
        }
        return null
    }
    
    /**
     * Parses Paytm SMS messages
     */
    fun parsePaytmSms(smsContent: String, dateTime: Date): ParsedTransaction? {
        val patterns = listOf(
            // Paytm UPI payment
            "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+transferred\\s+to\\s+([^\\s]+)\\s+via\\s+Paytm\\s+UPI\\s+Txn\\s+ID\\s*:?\\s*([A-Z0-9]+)".toRegex(),
            // Paytm wallet payment
            "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+from\\s+Paytm\\s+Wallet\\s+to\\s+([^\\s]+)\\s+Order\\s+ID\\s*:?\\s*([A-Z0-9]+)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(smsContent)
            if (match != null) {
                val amount = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: continue
                val recipient = if (match.groupValues.size > 2) match.groupValues[2].trim() else null
                val transactionId = if (match.groupValues.size > 3) match.groupValues[3] else null
                
                val paymentMethod = if (smsContent.contains("wallet", ignoreCase = true)) {
                    ParsedTransaction.METHOD_WALLET
                } else {
                    ParsedTransaction.METHOD_UPI
                }
                
                return ParsedTransaction(
                    amount = amount,
                    recipient = if (recipient?.contains("@") == true) recipient else null,
                    merchantName = if (recipient?.contains("@") != true) recipient else null,
                    transactionId = transactionId,
                    paymentMethod = paymentMethod,
                    dateTime = dateTime,
                    smsContent = smsContent,
                    sender = "PAYTM",
                    confidence = 0.95f
                )
            }
        }
        return null
    }
    
    private fun determineHdfcPaymentMethod(smsContent: String): String {
        return when {
            smsContent.contains("UPI", ignoreCase = true) -> ParsedTransaction.METHOD_UPI
            smsContent.contains("Credit Card", ignoreCase = true) -> ParsedTransaction.METHOD_CREDIT_CARD
            smsContent.contains("Debit", ignoreCase = true) -> ParsedTransaction.METHOD_DEBIT_CARD
            else -> ParsedTransaction.METHOD_OTHER
        }
    }
    
    private fun determineSbiPaymentMethod(smsContent: String): String {
        return when {
            smsContent.contains("UPI", ignoreCase = true) -> ParsedTransaction.METHOD_UPI
            smsContent.contains("Card", ignoreCase = true) -> ParsedTransaction.METHOD_DEBIT_CARD
            smsContent.contains("Net Banking", ignoreCase = true) -> ParsedTransaction.METHOD_NET_BANKING
            else -> ParsedTransaction.METHOD_OTHER
        }
    }
    
    private fun determineIciciPaymentMethod(smsContent: String): String {
        return when {
            smsContent.contains("UPI", ignoreCase = true) -> ParsedTransaction.METHOD_UPI
            smsContent.contains("Credit", ignoreCase = true) -> ParsedTransaction.METHOD_CREDIT_CARD
            smsContent.contains("Debit", ignoreCase = true) -> ParsedTransaction.METHOD_DEBIT_CARD
            else -> ParsedTransaction.METHOD_OTHER
        }
    }
}