package com.expensetracker.data.sms.util

import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.domain.model.Transaction
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class DuplicateDetectionServiceTest {
    
    private lateinit var duplicateDetectionService: DuplicateDetectionService
    
    @Before
    fun setup() {
        duplicateDetectionService = DuplicateDetectionService()
    }
    
    @Test
    fun `isDuplicate returns true for exact duplicate transactions`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val result = duplicateDetectionService.isDuplicate(
            parsedTransaction,
            listOf(existingTransaction)
        )
        
        assertTrue(result)
    }
    
    @Test
    fun `isDuplicate returns true for transactions within time window`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 200.0,
            recipient = "9876543210",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 200.0,
            recipient = "9876543210",
            dateTime = Date(baseTime.time + 2 * 60 * 1000) // 2 minutes later
        )
        
        val result = duplicateDetectionService.isDuplicate(
            parsedTransaction,
            listOf(existingTransaction)
        )
        
        assertTrue(result)
    }
    
    @Test
    fun `isDuplicate returns false for transactions outside time window`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = Date(baseTime.time + 10 * 60 * 1000) // 10 minutes later
        )
        
        val result = duplicateDetectionService.isDuplicate(
            parsedTransaction,
            listOf(existingTransaction)
        )
        
        assertFalse(result)
    }
    
    @Test
    fun `isDuplicate returns false for different amounts`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 200.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val result = duplicateDetectionService.isDuplicate(
            parsedTransaction,
            listOf(existingTransaction)
        )
        
        assertFalse(result)
    }
    
    @Test
    fun `isDuplicate returns false for different merchants`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 150.0,
            merchantName = "SWIGGY",
            dateTime = baseTime
        )
        
        val result = duplicateDetectionService.isDuplicate(
            parsedTransaction,
            listOf(existingTransaction)
        )
        
        assertFalse(result)
    }
    
    @Test
    fun `findPotentialDuplicates returns matching transactions`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 100.0,
            merchantName = "AMAZON",
            dateTime = baseTime
        )
        
        val existingTransactions = listOf(
            createTransaction(
                amount = 100.0,
                merchantName = "AMAZON",
                dateTime = Date(baseTime.time + 1 * 60 * 1000) // 1 minute later
            ),
            createTransaction(
                amount = 200.0,
                merchantName = "FLIPKART",
                dateTime = baseTime
            ),
            createTransaction(
                amount = 100.0,
                merchantName = "ZOMATO",
                dateTime = baseTime
            )
        )
        
        val result = duplicateDetectionService.findPotentialDuplicates(
            parsedTransaction,
            existingTransactions
        )
        
        assertEquals(1, result.size)
        assertEquals("AMAZON", result[0].merchantName)
    }
    
    @Test
    fun `areDuplicates returns true for duplicate parsed transactions`() {
        val baseTime = Date()
        val transaction1 = createParsedTransaction(
            amount = 250.0,
            recipient = "user@paytm",
            dateTime = baseTime
        )
        
        val transaction2 = createParsedTransaction(
            amount = 250.0,
            recipient = "user@paytm",
            dateTime = Date(baseTime.time + 3 * 60 * 1000) // 3 minutes later
        )
        
        val result = duplicateDetectionService.areDuplicates(transaction1, transaction2)
        
        assertTrue(result)
    }
    
    @Test
    fun `areDuplicates returns false for different parsed transactions`() {
        val baseTime = Date()
        val transaction1 = createParsedTransaction(
            amount = 250.0,
            recipient = "user@paytm",
            dateTime = baseTime
        )
        
        val transaction2 = createParsedTransaction(
            amount = 300.0,
            recipient = "user@phonepe",
            dateTime = baseTime
        )
        
        val result = duplicateDetectionService.areDuplicates(transaction1, transaction2)
        
        assertFalse(result)
    }
    
    @Test
    fun `removeDuplicates filters duplicate parsed transactions`() {
        val baseTime = Date()
        val transactions = listOf(
            createParsedTransaction(
                amount = 100.0,
                merchantName = "ZOMATO",
                dateTime = baseTime
            ),
            createParsedTransaction(
                amount = 100.0,
                merchantName = "ZOMATO",
                dateTime = Date(baseTime.time + 2 * 60 * 1000) // 2 minutes later - duplicate
            ),
            createParsedTransaction(
                amount = 200.0,
                merchantName = "SWIGGY",
                dateTime = baseTime
            ),
            createParsedTransaction(
                amount = 100.0,
                merchantName = "UBER",
                dateTime = baseTime
            )
        )
        
        val result = duplicateDetectionService.removeDuplicates(transactions)
        
        assertEquals(3, result.size) // Should remove 1 duplicate
        assertTrue(result.any { it.merchantName == "ZOMATO" })
        assertTrue(result.any { it.merchantName == "SWIGGY" })
        assertTrue(result.any { it.merchantName == "UBER" })
    }
    
    @Test
    fun `calculateDuplicateConfidence returns high confidence for exact matches`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            transactionId = "TXN123",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            transactionId = "TXN123",
            dateTime = baseTime
        )
        
        val confidence = duplicateDetectionService.calculateDuplicateConfidence(
            parsedTransaction,
            existingTransaction
        )
        
        assertTrue("Confidence should be high for exact match", confidence >= 0.9f)
    }
    
    @Test
    fun `calculateDuplicateConfidence returns medium confidence for partial matches`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = Date(baseTime.time + 4 * 60 * 1000) // 4 minutes later
        )
        
        val confidence = duplicateDetectionService.calculateDuplicateConfidence(
            parsedTransaction,
            existingTransaction
        )
        
        assertTrue("Confidence should be medium for partial match", confidence >= 0.5f && confidence < 0.9f)
    }
    
    @Test
    fun `calculateDuplicateConfidence returns low confidence for poor matches`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 200.0,
            merchantName = "SWIGGY",
            dateTime = Date(baseTime.time + 8 * 60 * 1000) // 8 minutes later
        )
        
        val confidence = duplicateDetectionService.calculateDuplicateConfidence(
            parsedTransaction,
            existingTransaction
        )
        
        assertTrue("Confidence should be low for poor match", confidence < 0.5f)
    }
    
    @Test
    fun `getDuplicateDetectionSummary provides comprehensive analysis`() {
        val baseTime = Date()
        val parsedTransactions = listOf(
            createParsedTransaction(
                amount = 100.0,
                merchantName = "ZOMATO",
                dateTime = baseTime
            ),
            createParsedTransaction(
                amount = 200.0,
                merchantName = "SWIGGY",
                dateTime = baseTime
            ),
            createParsedTransaction(
                amount = 300.0,
                merchantName = "UBER",
                dateTime = baseTime
            )
        )
        
        val existingTransactions = listOf(
            createTransaction(
                amount = 100.0,
                merchantName = "ZOMATO",
                dateTime = Date(baseTime.time + 1 * 60 * 1000) // 1 minute later - duplicate
            ),
            createTransaction(
                amount = 500.0,
                merchantName = "OLA",
                dateTime = baseTime
            )
        )
        
        val summary = duplicateDetectionService.getDuplicateDetectionSummary(
            parsedTransactions,
            existingTransactions
        )
        
        assertEquals(3, summary.totalParsed)
        assertEquals(2, summary.uniqueCount) // SWIGGY and UBER are unique
        assertEquals(1, summary.duplicateCount) // ZOMATO is duplicate
        assertTrue(summary.potentialDuplicates.containsKey(parsedTransactions[0])) // ZOMATO transaction
    }
    
    @Test
    fun `time window validation works correctly`() {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 100.0,
            merchantName = "TEST",
            dateTime = baseTime
        )
        
        val existingTransaction = createTransaction(
            amount = 100.0,
            merchantName = "TEST",
            dateTime = Date(baseTime.time + 2 * 60 * 1000) // 2 minutes later
        )
        
        // Test with 1 minute window (should not be duplicate)
        val result1 = duplicateDetectionService.isDuplicate(
            parsedTransaction,
            listOf(existingTransaction),
            timeWindowMs = 1 * 60 * 1000L
        )
        assertFalse(result1)
        
        // Test with 5 minute window (should be duplicate)
        val result2 = duplicateDetectionService.isDuplicate(
            parsedTransaction,
            listOf(existingTransaction),
            timeWindowMs = 5 * 60 * 1000L
        )
        assertTrue(result2)
    }
    
    private fun createParsedTransaction(
        amount: Double = 100.0,
        recipient: String? = null,
        merchantName: String? = "TEST_MERCHANT",
        transactionId: String? = null,
        paymentMethod: String = ParsedTransaction.METHOD_UPI,
        dateTime: Date = Date(),
        smsContent: String = "Test SMS content",
        sender: String = "TEST_SENDER"
    ): ParsedTransaction {
        return ParsedTransaction(
            amount = amount,
            recipient = recipient,
            merchantName = merchantName,
            transactionId = transactionId,
            paymentMethod = paymentMethod,
            dateTime = dateTime,
            smsContent = smsContent,
            sender = sender
        )
    }
    
    private fun createTransaction(
        amount: Double = 100.0,
        recipient: String? = null,
        merchantName: String? = "TEST_MERCHANT",
        transactionId: String? = null,
        paymentMethod: String = Transaction.PAYMENT_METHOD_UPI,
        dateTime: Date = Date()
    ): Transaction {
        return Transaction(
            amount = amount,
            recipient = recipient,
            merchantName = merchantName,
            transactionId = transactionId,
            paymentMethod = paymentMethod,
            dateTime = dateTime,
            category = null,
            notes = null,
            isCategorized = false,
            smsContent = "Test SMS content"
        )
    }
}