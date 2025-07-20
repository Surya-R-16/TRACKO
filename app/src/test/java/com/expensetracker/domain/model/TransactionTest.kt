package com.expensetracker.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class TransactionTest {
    
    @Test
    fun `getFormattedAmount returns correct format`() {
        val transaction = createSampleTransaction(amount = 150.50)
        assertEquals("₹150.50", transaction.getFormattedAmount())
    }
    
    @Test
    fun `getShortDescription returns merchant name when available`() {
        val transaction = createSampleTransaction(
            merchantName = "ZOMATO",
            recipient = "9876543210"
        )
        assertEquals("ZOMATO", transaction.getShortDescription())
    }
    
    @Test
    fun `getShortDescription returns recipient when merchant name is null`() {
        val transaction = createSampleTransaction(
            merchantName = null,
            recipient = "9876543210"
        )
        assertEquals("9876543210", transaction.getShortDescription())
    }
    
    @Test
    fun `getShortDescription returns Transaction when both are null`() {
        val transaction = createSampleTransaction(
            merchantName = null,
            recipient = null
        )
        assertEquals("Transaction", transaction.getShortDescription())
    }
    
    @Test
    fun `isDuplicateOf returns true for duplicate transactions within time window`() {
        val baseTime = Date()
        val transaction1 = createSampleTransaction(
            amount = 100.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        val transaction2 = createSampleTransaction(
            amount = 100.0,
            merchantName = "ZOMATO",
            dateTime = Date(baseTime.time + 2 * 60 * 1000) // 2 minutes later
        )
        
        assertTrue(transaction1.isDuplicateOf(transaction2))
    }
    
    @Test
    fun `isDuplicateOf returns false for transactions outside time window`() {
        val baseTime = Date()
        val transaction1 = createSampleTransaction(
            amount = 100.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        val transaction2 = createSampleTransaction(
            amount = 100.0,
            merchantName = "ZOMATO",
            dateTime = Date(baseTime.time + 10 * 60 * 1000) // 10 minutes later
        )
        
        assertFalse(transaction1.isDuplicateOf(transaction2))
    }
    
    @Test
    fun `isDuplicateOf returns false for different amounts`() {
        val baseTime = Date()
        val transaction1 = createSampleTransaction(
            amount = 100.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        val transaction2 = createSampleTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        assertFalse(transaction1.isDuplicateOf(transaction2))
    }
    
    @Test
    fun `isDuplicateOf returns false for different merchants`() {
        val baseTime = Date()
        val transaction1 = createSampleTransaction(
            amount = 100.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        val transaction2 = createSampleTransaction(
            amount = 100.0,
            merchantName = "SWIGGY",
            dateTime = baseTime
        )
        
        assertFalse(transaction1.isDuplicateOf(transaction2))
    }
    
    private fun createSampleTransaction(
        id: Long = 1,
        amount: Double = 100.0,
        recipient: String? = "9876543210",
        merchantName: String? = "ZOMATO",
        dateTime: Date = Date(),
        transactionId: String? = "TXN123",
        paymentMethod: String = Transaction.PAYMENT_METHOD_UPI,
        category: String? = null,
        notes: String? = null,
        isCategorized: Boolean = false,
        smsContent: String = "₹100 paid to ZOMATO via UPI"
    ): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            recipient = recipient,
            merchantName = merchantName,
            dateTime = dateTime,
            transactionId = transactionId,
            paymentMethod = paymentMethod,
            category = category,
            notes = notes,
            isCategorized = isCategorized,
            smsContent = smsContent
        )
    }
}