package com.expensetracker.data.sms.model

import java.util.Date

/**
 * Data class representing a parsed transaction from SMS
 */
data class ParsedTransaction(
    val amount: Double,
    val recipient: String?,
    val merchantName: String?,
    val transactionId: String?,
    val paymentMethod: String,
    val dateTime: Date,
    val smsContent: String,
    val sender: String,
    val confidence: Float = 1.0f // Confidence level of parsing (0.0 to 1.0)
) {
    companion object {
        // Payment method constants
        const val METHOD_UPI = "UPI"
        const val METHOD_DEBIT_CARD = "Debit Card"
        const val METHOD_CREDIT_CARD = "Credit Card"
        const val METHOD_NET_BANKING = "Net Banking"
        const val METHOD_WALLET = "Wallet"
        const val METHOD_OTHER = "Other"
    }
    
    /**
     * Returns the primary identifier (recipient or merchant)
     */
    fun getPrimaryIdentifier(): String? {
        return merchantName ?: recipient
    }
    
    /**
     * Returns true if this transaction has high confidence parsing
     */
    fun isHighConfidence(): Boolean = confidence >= 0.8f
    
    /**
     * Returns true if this transaction has sufficient data for storage
     */
    fun isValid(): Boolean {
        return amount > 0 && 
               (recipient != null || merchantName != null) &&
               paymentMethod.isNotBlank() &&
               smsContent.isNotBlank()
    }
}