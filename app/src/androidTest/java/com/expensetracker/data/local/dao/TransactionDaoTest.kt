package com.expensetracker.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.expensetracker.data.local.database.AppDatabase
import com.expensetracker.data.local.entity.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var transactionDao: TransactionDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        transactionDao = database.transactionDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetTransaction() = runTest {
        val transaction = createSampleTransaction()
        
        val id = transactionDao.insertTransaction(transaction)
        assertTrue(id > 0)
        
        val retrievedTransaction = transactionDao.getTransactionById(id)
        assertNotNull(retrievedTransaction)
        assertEquals(transaction.amount, retrievedTransaction!!.amount, 0.01)
        assertEquals(transaction.recipient, retrievedTransaction.recipient)
    }
    
    @Test
    fun getAllTransactions() = runTest {
        val transactions = listOf(
            createSampleTransaction(amount = 100.0),
            createSampleTransaction(amount = 200.0),
            createSampleTransaction(amount = 300.0)
        )
        
        transactionDao.insertTransactions(transactions)
        
        val allTransactions = transactionDao.getAllTransactions().first()
        assertEquals(3, allTransactions.size)
        
        // Should be ordered by date_time DESC
        assertTrue(allTransactions[0].dateTime >= allTransactions[1].dateTime)
    }
    
    @Test
    fun getUncategorizedTransactions() = runTest {
        val categorizedTransaction = createSampleTransaction(
            isCategorized = true,
            category = "Food & Dining"
        )
        val uncategorizedTransaction = createSampleTransaction(
            isCategorized = false,
            category = null
        )
        
        transactionDao.insertTransaction(categorizedTransaction)
        transactionDao.insertTransaction(uncategorizedTransaction)
        
        val uncategorized = transactionDao.getUncategorizedTransactions().first()
        assertEquals(1, uncategorized.size)
        assertFalse(uncategorized[0].isCategorized)
    }
    
    @Test
    fun searchTransactions() = runTest {
        val transactions = listOf(
            createSampleTransaction(recipient = "ZOMATO", merchantName = null),
            createSampleTransaction(recipient = "9876543210", merchantName = "SWIGGY"),
            createSampleTransaction(recipient = "1234567890", merchantName = "AMAZON")
        )
        
        transactionDao.insertTransactions(transactions)
        
        val searchResults = transactionDao.searchTransactions("ZOMATO").first()
        assertEquals(1, searchResults.size)
        assertEquals("ZOMATO", searchResults[0].recipient)
        
        val swiggyResults = transactionDao.searchTransactions("SWIGGY").first()
        assertEquals(1, swiggyResults.size)
        assertEquals("SWIGGY", swiggyResults[0].merchantName)
    }
    
    @Test
    fun getTransactionsByDateRange() = runTest {
        val baseDate = Date()
        val oldDate = Date(baseDate.time - 7 * 24 * 60 * 60 * 1000) // 7 days ago
        val recentDate = Date(baseDate.time - 1 * 24 * 60 * 60 * 1000) // 1 day ago
        
        val transactions = listOf(
            createSampleTransaction(dateTime = oldDate),
            createSampleTransaction(dateTime = recentDate),
            createSampleTransaction(dateTime = baseDate)
        )
        
        transactionDao.insertTransactions(transactions)
        
        val startDate = Date(baseDate.time - 2 * 24 * 60 * 60 * 1000) // 2 days ago
        val endDate = baseDate
        
        val filteredTransactions = transactionDao.getTransactionsByDateRange(startDate, endDate).first()
        assertEquals(2, filteredTransactions.size) // Should exclude the old transaction
    }
    
    @Test
    fun updateTransactionCategory() = runTest {
        val transaction = createSampleTransaction(isCategorized = false, category = null)
        val id = transactionDao.insertTransaction(transaction)
        
        transactionDao.updateTransactionCategory(id, "Food & Dining", "Lunch", Date())
        
        val updatedTransaction = transactionDao.getTransactionById(id)
        assertNotNull(updatedTransaction)
        assertEquals("Food & Dining", updatedTransaction!!.category)
        assertEquals("Lunch", updatedTransaction.notes)
        assertTrue(updatedTransaction.isCategorized)
    }
    
    @Test
    fun getTotalAmountByDateRange() = runTest {
        val baseDate = Date()
        val transactions = listOf(
            createSampleTransaction(amount = 100.0, dateTime = baseDate),
            createSampleTransaction(amount = 200.0, dateTime = baseDate),
            createSampleTransaction(amount = 300.0, dateTime = Date(baseDate.time - 7 * 24 * 60 * 60 * 1000))
        )
        
        transactionDao.insertTransactions(transactions)
        
        val startDate = Date(baseDate.time - 1 * 24 * 60 * 60 * 1000)
        val endDate = Date(baseDate.time + 1 * 24 * 60 * 60 * 1000)
        
        val totalAmount = transactionDao.getTotalAmountByDateRange(startDate, endDate)
        assertEquals(300.0, totalAmount ?: 0.0, 0.01) // Should sum 100 + 200
    }
    
    @Test
    fun findPotentialDuplicate() = runTest {
        val baseTime = Date()
        val originalTransaction = createSampleTransaction(
            amount = 150.0,
            recipient = "ZOMATO",
            dateTime = baseTime
        )
        
        transactionDao.insertTransaction(originalTransaction)
        
        // Test duplicate within time window
        val duplicate = transactionDao.findPotentialDuplicate(
            amount = 150.0,
            recipient = "ZOMATO",
            merchantName = null,
            dateTime = baseTime.time + 2 * 60 * 1000, // 2 minutes later
            timeWindowMs = 5 * 60 * 1000 // 5 minutes window
        )
        
        assertNotNull(duplicate)
        assertEquals(150.0, duplicate!!.amount, 0.01)
        
        // Test no duplicate outside time window
        val noDuplicate = transactionDao.findPotentialDuplicate(
            amount = 150.0,
            recipient = "ZOMATO",
            merchantName = null,
            dateTime = baseTime.time + 10 * 60 * 1000, // 10 minutes later
            timeWindowMs = 5 * 60 * 1000 // 5 minutes window
        )
        
        assertNull(noDuplicate)
    }
    
    private fun createSampleTransaction(
        amount: Double = 100.0,
        recipient: String? = "9876543210",
        merchantName: String? = null,
        dateTime: Date = Date(),
        paymentMethod: String = "UPI",
        category: String? = null,
        isCategorized: Boolean = false
    ): Transaction {
        return Transaction(
            amount = amount,
            recipient = recipient,
            merchantName = merchantName,
            dateTime = dateTime,
            transactionId = "TXN${System.currentTimeMillis()}",
            paymentMethod = paymentMethod,
            category = category,
            notes = null,
            isCategorized = isCategorized,
            smsContent = "₹$amount paid to ${recipient ?: merchantName} via $paymentMethod"
        )
    }
}