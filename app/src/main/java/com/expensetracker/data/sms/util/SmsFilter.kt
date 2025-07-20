package com.expensetracker.data.sms.util

import com.expensetracker.data.sms.model.SmsMessage

/**
 * Utility class for filtering SMS messages
 */
object SmsFilter {
    
    // Transaction keywords that indicate a financial transaction
    private val TRANSACTION_KEYWORDS = listOf(
        "paid", "debited", "sent", "transferred", "credited", "received",
        "transaction", "payment", "purchase", "spent", "withdrawn",
        "refund", "cashback", "reward", "balance", "amount"
    )
    
    // Currency symbols and formats
    private val CURRENCY_PATTERNS = listOf(
        "₹", "rs.", "rs ", "inr", "rupees"
    )
    
    // UPI and payment related keywords
    private val PAYMENT_KEYWORDS = listOf(
        "upi", "gpay", "phonepe", "paytm", "bhim", "whatsapp pay",
        "debit card", "credit card", "net banking", "wallet",
        "ref no", "reference", "txn", "transaction id"
    )
    
    /**
     * Filters SMS messages that are likely to be transaction messages
     */
    fun filterTransactionSms(messages: List<SmsMessage>): List<SmsMessage> {
        return messages.filter { isTransactionSms(it) }
    }
    
    /**
     * Checks if an SMS message is likely to be a transaction message
     */
    fun isTransactionSms(message: SmsMessage): Boolean {
        val body = message.body.lowercase()
        val sender = message.getNormalizedSender()
        
        // Check if sender is from a known financial institution
        if (isFinancialSender(sender)) {
            return true
        }
        
        // Check if message contains transaction keywords
        val hasTransactionKeyword = TRANSACTION_KEYWORDS.any { keyword ->
            body.contains(keyword)
        }
        
        // Check if message contains currency information
        val hasCurrencyInfo = CURRENCY_PATTERNS.any { pattern ->
            body.contains(pattern)
        }
        
        // Check if message contains payment keywords
        val hasPaymentKeyword = PAYMENT_KEYWORDS.any { keyword ->
            body.contains(keyword)
        }
        
        // Message is likely a transaction if it has:
        // 1. Transaction keyword + currency info, OR
        // 2. Payment keyword + currency info, OR
        // 3. Multiple transaction indicators
        return (hasTransactionKeyword && hasCurrencyInfo) ||
               (hasPaymentKeyword && hasCurrencyInfo) ||
               (hasTransactionKeyword && hasPaymentKeyword)
    }
    
    /**
     * Checks if the sender is from a known financial institution
     */
    fun isFinancialSender(sender: String): Boolean {
        val normalizedSender = sender.uppercase()
        
        val financialSenders = listOf(
            // Banks
            "HDFC", "HDFCBK", "SBI", "SBICARD", "ICICI", "ICICIBK", "AXIS", "AXISBK",
            "KOTAK", "KOTAKBK", "PNB", "PNBBK", "BOB", "BOBBK", "CANARA", "CANBK",
            "UNION", "UNIONBK", "INDIAN", "INDBK", "FEDERAL", "FEDBK",
            
            // Payment Apps
            "GPAY", "GOOGLEPAY", "PHONEPE", "PAYTM", "AMAZONPAY", "MOBIKWIK",
            "FREECHARGE", "PAYPAL", "BHIM", "WHATSAPP",
            
            // Credit Cards
            "AMEX", "CITI", "CITIBANK", "HSBC", "STANCHART", "YESBANK",
            
            // Common transaction senders
            "AMAZON", "FLIPKART", "ZOMATO", "SWIGGY", "UBER", "OLA"
        )
        
        return financialSenders.any { financialSender ->
            normalizedSender.contains(financialSender)
        }
    }
    
    /**
     * Filters SMS messages by date range
     */
    fun filterByDateRange(
        messages: List<SmsMessage>,
        startTimeMs: Long,
        endTimeMs: Long
    ): List<SmsMessage> {
        return messages.filter { message ->
            message.date.time in startTimeMs..endTimeMs
        }
    }
    
    /**
     * Filters SMS messages by specific senders
     */
    fun filterBySenders(
        messages: List<SmsMessage>,
        senders: List<String>
    ): List<SmsMessage> {
        val normalizedSenders = senders.map { it.uppercase() }
        return messages.filter { message ->
            val normalizedSender = message.getNormalizedSender()
            normalizedSenders.any { sender ->
                normalizedSender.contains(sender)
            }
        }
    }
    
    /**
     * Removes duplicate SMS messages based on content and timestamp
     */
    fun removeDuplicates(messages: List<SmsMessage>): List<SmsMessage> {
        return messages.distinctBy { message ->
            // Create a unique key based on sender, body, and approximate time
            val timeWindow = message.date.time / (5 * 60 * 1000) // 5-minute windows
            "${message.address}_${message.body.hashCode()}_$timeWindow"
        }
    }
    
    /**
     * Sorts SMS messages by date (newest first)
     */
    fun sortByDateDesc(messages: List<SmsMessage>): List<SmsMessage> {
        return messages.sortedByDescending { it.date }
    }
}