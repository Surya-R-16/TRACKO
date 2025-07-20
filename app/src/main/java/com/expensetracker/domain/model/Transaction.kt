package com.expensetracker.domain.model

import java.util.Date

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val recipient: String?,
    val merchantName: String?,
    val dateTime: Date,
    val transactionId: String?,
    val paymentMethod: String,
    val category: String?,
    val notes: String?,
    val isCategorized: Boolean = false,
    val smsContent: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    companion object {
        // Payment method constants
        const val PAYMENT_METHOD_UPI = "UPI"
        const val PAYMENT_METHOD_DEBIT_CARD = "Debit Card"
        const val PAYMENT_METHOD_CREDIT_CARD = "Credit Card"
        const val PAYMENT_METHOD_NET_BANKING = "Net Banking"
        const val PAYMENT_METHOD_WALLET = "Wallet"
        const val PAYMENT_METHOD_OTHER = "Other"
        
        // Category constants
        const val CATEGORY_FOOD = "Food & Dining"
        const val CATEGORY_TRANSPORTATION = "Transportation"
        const val CATEGORY_SHOPPING = "Shopping"
        const val CATEGORY_ENTERTAINMENT = "Entertainment"
        const val CATEGORY_BILLS = "Bills & Utilities"
        const val CATEGORY_HEALTH = "Health & Medical"
        const val CATEGORY_EDUCATION = "Education"
        const val CATEGORY_PERSONAL_CARE = "Personal Care"
        const val CATEGORY_OTHER = "Other"
    }
    
    /**
     * Returns a formatted amount string with currency symbol
     */
    fun getFormattedAmount(): String {
        return "₹${String.format("%.2f", amount)}"
    }
    
    /**
     * Returns a short description of the transaction
     */
    fun getShortDescription(): String {
        return when {
            !merchantName.isNullOrBlank() -> merchantName
            !recipient.isNullOrBlank() -> recipient
            else -> "Transaction"
        }
    }
    
    /**
     * Checks if this transaction is a duplicate of another transaction
     * Based on amount, recipient/merchant, and time window (5 minutes)
     */
    fun isDuplicateOf(other: Transaction, timeWindowMinutes: Long = 5): Boolean {
        val timeDifference = kotlin.math.abs(dateTime.time - other.dateTime.time)
        val timeWindowMs = timeWindowMinutes * 60 * 1000
        
        return amount == other.amount &&
                getShortDescription() == other.getShortDescription() &&
                timeDifference <= timeWindowMs
    }
}