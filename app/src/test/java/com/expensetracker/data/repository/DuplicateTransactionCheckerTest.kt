package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.local.entity.Transaction as TransactionEntity
import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.data.sms.util.DuplicateDetectionService
import com.expensetracker.domain.model.Transaction
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class DuplicateTransactionCheckerTest {
    
    private lateinit var transactionDao: TransactionDao
    private lateinit var duplicateDetectionService: DuplicateDetectionService
    private lateinit var duplicateTransactionChecker: DuplicateTransactionChecker
    
    @Before
    fun setup() {
        transactionDao = mockk()
        duplicateDetectionService = DuplicateDetectionService()
        duplicateTransactionChecker = DuplicateTransactionChecker(transactionDao, duplicateDetectionService)
    }
    
    @Test
    fun `isDuplicateTransaction returns true when duplicate exists in database`() = runTest {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = baseTime
        )
        
        val existingEntity = createTransactionEntity(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = Date(baseTime.time + 2 * 60 * 1000) // 2 minutes later
        )
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = 150.0,
                recipient = null,
                merchantName = "ZOMATO",
                dateTime = baseTime.time,
                timeWindowMs = DuplicateDetectionService.DEFAULT_TIME_WINDOW_MS
            )
        } returns existingEntity
        
        val result = duplicateTransactionChecker.isDuplicateTransaction(parsedTransaction)
        
        assertTrue(result)
    }
    
    @Test
    fun `isDuplicateTransaction returns false when no duplicate exists`() = runTest {
        val parsedTransaction = createParsedTransaction(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = Date()
        )
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = any(),
                recipient = any(),
                merchantName = any(),
                dateTime = any(),
                timeWindowMs = any()
            )
        } returns null
        
        val result = duplicateTransactionChecker.isDuplicateTransaction(parsedTransaction)
        
        assertFalse(result)
    }
    
    @Test
    fun `findPotentialDuplicates returns matching transactions`() = runTest {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 200.0,
            recipient = "9876543210",
            dateTime = baseTime
        )
        
        val existingEntity = createTransactionEntity(
            amount = 200.0,
            recipient = "9876543210",
            dateTime = Date(baseTime.time + 1 * 60 * 1000) // 1 minute later
        )
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = 200.0,
                recipient = "9876543210",
                merchantName = null,
                dateTime = baseTime.time,
                timeWindowMs = DuplicateDetectionService.DEFAULT_TIME_WINDOW_MS
            )
        } returns existingEntity
        
        val result = duplicateTransactionChecker.findPotentialDuplicates(parsedTransaction)
        
        assertEquals(1, result.size)
        assertEquals(200.0, result[0].amount, 0.01)
        assertEquals("9876543210", result[0].recipient)
    }
    
    @Test
    fun `findPotentialDuplicates returns empty list when no duplicates found`() = runTest {
        val parsedTransaction = createParsedTransaction(
            amount = 300.0,
            merchantName = "UBER",
            dateTime = Date()
        )
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = any(),
                recipient = any(),
                merchantName = any(),
                dateTime = any(),
                timeWindowMs = any()
            )
        } returns null
        
        val result = duplicateTransactionChecker.findPotentialDuplicates(parsedTransaction)
        
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `filterDuplicates removes duplicate transactions`() = runTest {
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
        
        val existingEntity = createTransactionEntity(
            amount = 100.0,
            merchantName = "ZOMATO",
            dateTime = Date(baseTime.time + 1 * 60 * 1000)
        )
        
        // Mock that only ZOMATO transaction has a duplicate
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = 100.0,
                recipient = null,
                merchantName = "ZOMATO",
                dateTime = baseTime.time,
                timeWindowMs = any()
            )
        } returns existingEntity
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = 200.0,
                recipient = null,
                merchantName = "SWIGGY",
                dateTime = baseTime.time,
                timeWindowMs = any()
            )
        } returns null
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = 300.0,
                recipient = null,
                merchantName = "UBER",
                dateTime = baseTime.time,
                timeWindowMs = any()
            )
        } returns null
        
        val result = duplicateTransactionChecker.filterDuplicates(parsedTransactions)
        
        assertEquals(2, result.size) // Should remove ZOMATO duplicate
        assertTrue(result.any { it.merchantName == "SWIGGY" })
        assertTrue(result.any { it.merchantName == "UBER" })
        assertFalse(result.any { it.merchantName == "ZOMATO" })
    }
    
    @Test
    fun `analyzeDuplicates provides comprehensive analysis`() = runTest {
        val baseTime = Date()
        val parsedTransactions = listOf(
            createParsedTransaction(
                amount = 150.0,
                merchantName = "ZOMATO",
                dateTime = baseTime
            ),
            createParsedTransaction(
                amount = 250.0,
                merchantName = "SWIGGY",
                dateTime = baseTime
            )
        )
        
        val existingEntity = createTransactionEntity(
            amount = 150.0,
            merchantName = "ZOMATO",
            dateTime = Date(baseTime.time + 2 * 60 * 1000)
        )
        
        // Mock ZOMATO as duplicate, SWIGGY as unique
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = 150.0,
                recipient = null,
                merchantName = "ZOMATO",
                dateTime = baseTime.time,
                timeWindowMs = any()
            )
        } returns existingEntity
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = 250.0,
                recipient = null,
                merchantName = "SWIGGY",
                dateTime = baseTime.time,
                timeWindowMs = any()
            )
        } returns null
        
        val result = duplicateTransactionChecker.analyzeDuplicates(parsedTransactions)
        
        assertEquals(2, result.totalTransactions)
        assertEquals(1, result.uniqueCount)
        assertEquals(1, result.duplicateCount)
        assertEquals(50.0f, result.duplicatePercentage, 0.01f)
        assertTrue(result.hasDuplicates())
        assertFalse(result.hasNoDuplicates())
        
        assertTrue(result.uniqueTransactions.any { it.merchantName == "SWIGGY" })
        assertTrue(result.duplicateTransactions.any { it.merchantName == "ZOMATO" })
        assertTrue(result.duplicateDetails.isNotEmpty())
    }
    
    @Test
    fun `calculateDuplicateConfidence returns appropriate confidence score`() = runTest {
        val baseTime = Date()
        val parsedTransaction = createParsedTransaction(
            amount = 180.0,
            merchantName = "AMAZON",
            transactionId = "TXN123",
            dateTime = baseTime
        )
        
        val existingEntity = createTransactionEntity(
            amount = 180.0,
            merchantName = "AMAZON",
            transactionId = "TXN123",
            dateTime = baseTime
        )
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = 180.0,
                recipient = null,
                merchantName = "AMAZON",
                dateTime = baseTime.time,
                timeWindowMs = any()
            )
        } returns existingEntity
        
        val confidence = duplicateTransactionChecker.calculateDuplicateConfidence(parsedTransaction)
        
        assertTrue("Confidence should be high for exact match", confidence >= 0.8f)
    }
    
    @Test
    fun `calculateDuplicateConfidence returns zero when no duplicates found`() = runTest {
        val parsedTransaction = createParsedTransaction(
            amount = 500.0,
            merchantName = "UNIQUE_MERCHANT",
            dateTime = Date()
        )
        
        coEvery {
            transactionDao.findPotentialDuplicate(
                amount = any(),
                recipient = any(),
                merchantName = any(),
                dateTime = any(),
                timeWindowMs = any()
            )
        } returns null
        
        val confidence = duplicateTransactionChecker.calculateDuplicateConfidence(parsedTransaction)
        
        assertEquals(0.0f, confidence, 0.01f)
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
    
    private fun createTransactionEntity(
        amount: Double = 100.0,
        recipient: String? = null,
        merchantName: String? = "TEST_MERCHANT",
        transactionId: String? = null,
        paymentMethod: String = "UPI",
        dateTime: Date = Date()
    ): TransactionEntity {
        return TransactionEntity(
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