package com.expensetracker.data.sms.util

/**
 * Utility object containing regex patterns for different SMS formats
 */
object SmsPatterns {
    
    /**
     * Common UPI transaction patterns
     */
    val UPI_PATTERNS = listOf(
        // GPay patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+to\\s+([^\\s]+)\\s+via\\s+UPI".toRegex(RegexOption.IGNORE_CASE),
        "You\\s+paid\\s+₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+to\\s+([^\\s]+)".toRegex(RegexOption.IGNORE_CASE),
        
        // PhonePe patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+sent\\s+to\\s+([^\\s]+)\\s+via\\s+PhonePe".toRegex(RegexOption.IGNORE_CASE),
        "You\\s+sent\\s+₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+to\\s+([^\\s]+)".toRegex(RegexOption.IGNORE_CASE),
        
        // Paytm patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+transferred\\s+to\\s+([^\\s]+)\\s+via\\s+Paytm".toRegex(RegexOption.IGNORE_CASE),
        
        // Generic UPI patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+(?:paid|sent|transferred)\\s+(?:to\\s+)?([^\\s]+)\\s+via\\s+UPI".toRegex(RegexOption.IGNORE_CASE)
    )
    
    /**
     * Bank debit/credit card patterns
     */
    val CARD_PATTERNS = listOf(
        // HDFC patterns
        "Rs\\.(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+debited\\s+from\\s+.*?\\s+at\\s+([A-Z][A-Z0-9\\s]+)".toRegex(),
        "INR\\s+(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+spent\\s+on\\s+([A-Z][A-Z0-9\\s]+)\\s+using.*?Card".toRegex(),
        
        // SBI patterns
        "Rs\\s+(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+debited\\s+from\\s+.*?\\s+on\\s+([A-Z][A-Z0-9\\s]+)".toRegex(),
        
        // ICICI patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+debited\\s+from\\s+.*?\\s+at\\s+([A-Z][A-Z0-9\\s]+)".toRegex(),
        
        // Generic card patterns
        "(?:Rs\\.?|₹|INR)\\s*(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+(?:debited|spent)\\s+.*?(?:at|on)\\s+([A-Z][A-Z0-9\\s]+)".toRegex(RegexOption.IGNORE_CASE)
    )
    
    /**
     * E-commerce and merchant patterns
     */
    val MERCHANT_PATTERNS = listOf(
        // Amazon patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+for\\s+Amazon\\s+order".toRegex(RegexOption.IGNORE_CASE),
        "Payment\\s+of\\s+₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+made\\s+to\\s+Amazon".toRegex(RegexOption.IGNORE_CASE),
        
        // Flipkart patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+for\\s+Flipkart\\s+order".toRegex(RegexOption.IGNORE_CASE),
        
        // Zomato patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+to\\s+Zomato".toRegex(RegexOption.IGNORE_CASE),
        "You\\s+paid\\s+₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+for\\s+your\\s+Zomato\\s+order".toRegex(RegexOption.IGNORE_CASE),
        
        // Swiggy patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+to\\s+Swiggy".toRegex(RegexOption.IGNORE_CASE),
        
        // Uber patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+for\\s+Uber\\s+trip".toRegex(RegexOption.IGNORE_CASE),
        
        // Ola patterns
        "₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)\\s+paid\\s+for\\s+Ola\\s+ride".toRegex(RegexOption.IGNORE_CASE)
    )
    
    /**
     * Transaction reference patterns
     */
    val REFERENCE_PATTERNS = listOf(
        "UPI\\s+Ref\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
        "Ref\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
        "Reference\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
        "TXN\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
        "Transaction\\s+ID\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE),
        "Order\\s+ID\\s*:?\\s*([A-Z0-9]{8,20})".toRegex(RegexOption.IGNORE_CASE)
    )
    
    /**
     * Date and time patterns in SMS
     */
    val DATE_TIME_PATTERNS = listOf(
        "(\\d{1,2})-(\\d{1,2})-(\\d{2,4})\\s+(\\d{1,2}):(\\d{2})".toRegex(),
        "(\\d{1,2})/(\\d{1,2})/(\\d{2,4})\\s+(\\d{1,2}):(\\d{2})".toRegex(),
        "on\\s+(\\d{1,2})-(\\w{3})-(\\d{2,4})".toRegex(RegexOption.IGNORE_CASE),
        "at\\s+(\\d{1,2}):(\\d{2})\\s+(AM|PM)".toRegex(RegexOption.IGNORE_CASE)
    )
    
    /**
     * Balance information patterns
     */
    val BALANCE_PATTERNS = listOf(
        "Available\\s+balance\\s*:?\\s*₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)".toRegex(RegexOption.IGNORE_CASE),
        "Balance\\s*:?\\s*₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)".toRegex(RegexOption.IGNORE_CASE),
        "Bal\\s*:?\\s*₹(\\d+(?:,\\d+)*(?:\\.\\d{2})?)".toRegex(RegexOption.IGNORE_CASE)
    )
    
    /**
     * Account information patterns
     */
    val ACCOUNT_PATTERNS = listOf(
        "account\\s+ending\\s+(\\d{4})".toRegex(RegexOption.IGNORE_CASE),
        "A/c\\s+ending\\s+(\\d{4})".toRegex(RegexOption.IGNORE_CASE),
        "card\\s+ending\\s+(\\d{4})".toRegex(RegexOption.IGNORE_CASE),
        "from\\s+account\\s+.*?(\\d{4})".toRegex(RegexOption.IGNORE_CASE)
    )
}