package com.expensetracker.domain.util

import com.expensetracker.domain.model.Transaction

/**
 * Utility class for validating transaction data
 */
object TransactionValidator {
    
    /**
     * Validates a transaction and returns validation result
     */
    fun validateTransaction(transaction: Transaction): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate amount
        if (transaction.amount <= 0) {
            errors.add("Amount must be greater than zero")
        }
        
        if (transaction.amount > 1000000) {
            errors.add("Amount seems unusually high")
        }
        
        // Validate payment method
        if (transaction.paymentMethod.isBlank()) {
            errors.add("Payment method is required")
        }
        
        // Validate SMS content
        if (transaction.smsContent.isBlank()) {
            errors.add("SMS content is required")
        }
        
        // Validate recipient or merchant name
        if (transaction.recipient.isNullOrBlank() && transaction.merchantName.isNullOrBlank()) {
            errors.add("Either recipient or merchant name is required")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Checks if a transaction amount is within reasonable bounds
     */
    fun isAmountReasonable(amount: Double): Boolean {
        return amount > 0 && amount <= 1000000
    }
    
    /**
     * Checks if payment method is valid
     */
    fun isValidPaymentMethod(paymentMethod: String): Boolean {
        val validMethods = listOf(
            Transaction.PAYMENT_METHOD_UPI,
            Transaction.PAYMENT_METHOD_DEBIT_CARD,
            Transaction.PAYMENT_METHOD_CREDIT_CARD,
            Transaction.PAYMENT_METHOD_NET_BANKING,
            Transaction.PAYMENT_METHOD_WALLET,
            Transaction.PAYMENT_METHOD_OTHER
        )
        return paymentMethod in validMethods
    }
    
    /**
     * Checks if category is valid
     */
    fun isValidCategory(category: String?): Boolean {
        if (category.isNullOrBlank()) return true // Category can be null for uncategorized
        
        val validCategories = listOf(
            Transaction.CATEGORY_FOOD,
            Transaction.CATEGORY_TRANSPORTATION,
            Transaction.CATEGORY_SHOPPING,
            Transaction.CATEGORY_ENTERTAINMENT,
            Transaction.CATEGORY_BILLS,
            Transaction.CATEGORY_HEALTH,
            Transaction.CATEGORY_EDUCATION,
            Transaction.CATEGORY_PERSONAL_CARE,
            Transaction.CATEGORY_OTHER
        )
        return category in validCategories
    }
}

/**
 * Data class representing validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)