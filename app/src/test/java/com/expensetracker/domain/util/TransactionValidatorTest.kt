package com.expensetracker.domain.util

import com.expensetracker.domain.model.Transaction
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class TransactionValidatorTest {
    
    @Test
    fun `validateTransaction returns valid for correct transaction`() {
        val transaction = createValidTransaction()
        val result = TransactionValidator.validateTransaction(transaction)
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `validateTransaction returns invalid for zero amount`() {
        val transaction = createValidTransaction().copy(amount = 0.0)
        val result = TransactionValidator.validateTransaction(transaction)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Amount must be greater than zero"))
    }
    
    @Test
    fun `validateTransaction returns invalid for negative amount`() {
        val transaction = createValidTransaction().copy(amount = -100.0)
        val result = TransactionValidator.validateTransaction(transaction)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Amount must be greater than zero"))
    }
    
    @Test
    fun `validateTransaction returns warning for unusually high amount`() {
        val transaction = createValidTransaction().copy(amount = 2000000.0)
        val result = TransactionValidator.validateTransaction(transaction)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Amount seems unusually high"))
    }
    
    @Test
    fun `validateTransaction returns invalid for blank payment method`() {
        val transaction = createValidTransaction().copy(paymentMethod = "")
        val result = TransactionValidator.validateTransaction(transaction)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Payment method is required"))
    }
    
    @Test
    fun `validateTransaction returns invalid for blank SMS content`() {
        val transaction = createValidTransaction().copy(smsContent = "")
        val result = TransactionValidator.validateTransaction(transaction)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("SMS content is required"))
    }
    
    @Test
    fun `validateTransaction returns invalid when both recipient and merchant are null`() {
        val transaction = createValidTransaction().copy(
            recipient = null,
            merchantName = null
        )
        val result = TransactionValidator.validateTransaction(transaction)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Either recipient or merchant name is required"))
    }
    
    @Test
    fun `isAmountReasonable returns true for valid amounts`() {
        assertTrue(TransactionValidator.isAmountReasonable(100.0))
        assertTrue(TransactionValidator.isAmountReasonable(1.0))
        assertTrue(TransactionValidator.isAmountReasonable(50000.0))
    }
    
    @Test
    fun `isAmountReasonable returns false for invalid amounts`() {
        assertFalse(TransactionValidator.isAmountReasonable(0.0))
        assertFalse(TransactionValidator.isAmountReasonable(-100.0))
        assertFalse(TransactionValidator.isAmountReasonable(2000000.0))
    }
    
    @Test
    fun `isValidPaymentMethod returns true for valid methods`() {
        assertTrue(TransactionValidator.isValidPaymentMethod(Transaction.PAYMENT_METHOD_UPI))
        assertTrue(TransactionValidator.isValidPaymentMethod(Transaction.PAYMENT_METHOD_DEBIT_CARD))
        assertTrue(TransactionValidator.isValidPaymentMethod(Transaction.PAYMENT_METHOD_CREDIT_CARD))
        assertTrue(TransactionValidator.isValidPaymentMethod(Transaction.PAYMENT_METHOD_NET_BANKING))
        assertTrue(TransactionValidator.isValidPaymentMethod(Transaction.PAYMENT_METHOD_WALLET))
        assertTrue(TransactionValidator.isValidPaymentMethod(Transaction.PAYMENT_METHOD_OTHER))
    }
    
    @Test
    fun `isValidPaymentMethod returns false for invalid methods`() {
        assertFalse(TransactionValidator.isValidPaymentMethod("INVALID_METHOD"))
        assertFalse(TransactionValidator.isValidPaymentMethod(""))
    }
    
    @Test
    fun `isValidCategory returns true for valid categories`() {
        assertTrue(TransactionValidator.isValidCategory(Transaction.CATEGORY_FOOD))
        assertTrue(TransactionValidator.isValidCategory(Transaction.CATEGORY_TRANSPORTATION))
        assertTrue(TransactionValidator.isValidCategory(Transaction.CATEGORY_SHOPPING))
        assertTrue(TransactionValidator.isValidCategory(null)) // null is valid for uncategorized
    }
    
    @Test
    fun `isValidCategory returns false for invalid categories`() {
        assertFalse(TransactionValidator.isValidCategory("INVALID_CATEGORY"))
        assertFalse(TransactionValidator.isValidCategory(""))
    }
    
    private fun createValidTransaction(): Transaction {
        return Transaction(
            id = 1,
            amount = 100.0,
            recipient = "9876543210",
            merchantName = "ZOMATO",
            dateTime = Date(),
            transactionId = "TXN123",
            paymentMethod = Transaction.PAYMENT_METHOD_UPI,
            category = Transaction.CATEGORY_FOOD,
            notes = "Lunch order",
            isCategorized = true,
            smsContent = "₹100 paid to ZOMATO via UPI"
        )
    }
}