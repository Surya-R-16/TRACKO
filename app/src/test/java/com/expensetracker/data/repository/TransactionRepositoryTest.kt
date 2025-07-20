package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.local.entity.Transaction as TransactionEntity
import com.expensetracker.data.permission.PermissionManager
import com.expensetracker.data.repository.model.ImportErrorType
import com.expensetracker.data.sms.SmsContentProvider
import com.expensetracker.data.sms.SmsParserService
import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.data.sms.model.SmsMessage
import com.expensetracker.data.sms.util.DuplicateDetectionSummary
import com.expensetracker.domain.model.Transaction
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class TransactionRepositoryTest {
    
    private lateinit var transactionDao: TransactionDao
    private lateinit var smsContentProvider: SmsContentProvider
    private lateinit var smsParserService: SmsParserService
    private lateinit var duplicateTransactionChecker: DuplicateTransactionChecker
    private lateinit var permissionManager: PermissionManager
    private lateinit var transactionRepository: TransactionRepository
    
    @Before
    fun setup() {
        transactionDao = mockk()
        smsContentProvider = mockk()
        smsParserService = mockk()
        duplicateTransactionChecker = mockk()
        permissionManager = mockk()
        
        transactionRepository = TransactionRepositoryImpl(
            transactionDao,
            smsContentProvider,
            smsParserService,
            duplicateTransactionChecker,
            permissionManager
        )
    }
    
    @Test
    fun `insertTransaction inserts valid transaction successfully`() = runTest {
        val transaction = createValidTransaction()
        val expectedId = 1L
        
        coEvery { transactionDao.insertTransaction(any()) } returns expectedId
        
        val result = transactionRepository.insertTransaction(transaction)
        
        assertEquals(expectedId, result)
        coVerify { transactionDao.insertTransaction(any()) }
    }
    
    @Test
    fun `insertTransaction throws exception for invalid transaction`() = runTest {
        val invalidTransaction = createValidTransaction().copy(amount = -100.0) // Invalid amount
        
        try {
            transactionRepository.insertTransaction(invalidTransaction)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid transaction") == true)
        }
        
        coVerify(exactly = 0) { transactionDao.insertTransaction(any()) }
    }
    
    @Test
    fun `getAllTransactions returns flow of domain transactions`() = runTest {
        val transactionEntities = listOf(createTransactionEntity())
        
        every { transactionDao.getAllTransactions() } returns flowOf(transactionEntities)
        
        val result = transactionRepository.getAllTransactions()
        
        // Verify the flow emits the correct data
        result.collect { transactions ->
            assertEquals(1, transactions.size)
            assertEquals(100.0, transactions[0].amount, 0.01)
        }
    }
    
    @Test
    fun `getUncategorizedTransactions returns only uncategorized transactions`() = runTest {
        val uncategorizedEntity = createTransactionEntity(isCategorized = false)
        
        every { transactionDao.getUncategorizedTransactions() } returns flowOf(listOf(uncategorizedEntity))
        
        val result = transactionRepository.getUncategorizedTransactions()
        
        result.collect { transactions ->
            assertEquals(1, transactions.size)
            assertFalse(transactions[0].isCategorized)
        }
    }
    
    @Test
    fun `categorizeTransaction updates transaction category`() = runTest {
        val transactionId = 1L
        val category = "Food & Dining"
        val notes = "Lunch"
        
        coEvery { transactionDao.updateTransactionCategory(any(), any(), any(), any()) } just Runs
        
        transactionRepository.categorizeTransaction(transactionId, category, notes)
        
        coVerify { transactionDao.updateTransactionCategory(transactionId, category, notes, any()) }
    }
    
    @Test
    fun `categorizeMultipleTransactions updates multiple transactions`() = runTest {
        val transactionIds = listOf(1L, 2L, 3L)
        val category = "Transportation"
        
        coEvery { transactionDao.updateMultipleTransactionCategories(any(), any(), any()) } just Runs
        
        transactionRepository.categorizeMultipleTransactions(transactionIds, category)
        
        coVerify { transactionDao.updateMultipleTransactionCategories(transactionIds, category, any()) }
    }
    
    @Test
    fun `importTransactionsFromSms returns permission error when SMS permission denied`() = runTest {
        every { permissionManager.hasReadSmsPermission() } returns false
        
        val result = transactionRepository.importTransactionsFromSms()
        
        assertEquals(0, result.totalSmsProcessed)
        assertEquals(0, result.transactionsImported)
        assertTrue(result.hasErrors)
        assertEquals(ImportErrorType.PERMISSION_DENIED, result.errors[0].type)
    }
    
    @Test
    fun `importTransactionsFromSms successfully imports transactions when permission granted`() = runTest {
        val smsMessages = listOf(createSmsMessage())
        val parsedTransactions = listOf(createParsedTransaction())
        
        every { permissionManager.hasReadSmsPermission() } returns true
        coEvery { smsContentProvider.getFinancialSmsMessages() } returns smsMessages
        coEvery { smsParserService.parseTransactionSmsMessages(any()) } returns parsedTransactions
        coEvery { transactionDao.insertTransactions(any()) } returns listOf(1L)
        
        val result = transactionRepository.importTransactionsFromSms()
        
        assertEquals(1, result.totalSmsProcessed)
        assertEquals(1, result.transactionsParsed)
        assertEquals(1, result.transactionsImported)
        assertEquals(0, result.duplicatesFound)
        assertFalse(result.hasErrors)
        assertTrue(result.isSuccessful)
    }
    
    @Test
    fun `importTransactionsFromSmsWithDuplicateCheck filters duplicates`() = runTest {
        val smsMessages = listOf(createSmsMessage(), createSmsMessage())
        val duplicateAnalysis = DuplicateDetectionSummary(
            totalParsed = 2,
            uniqueTransactions = listOf(createParsedTransaction()),
            duplicateTransactions = listOf(createParsedTransaction()),
            potentialDuplicates = emptyMap()
        )
        
        every { permissionManager.hasReadSmsPermission() } returns true
        coEvery { smsContentProvider.getFinancialSmsMessages() } returns smsMessages
        coEvery { smsParserService.parseAndFilterDuplicates(any(), any()) } returns duplicateAnalysis
        coEvery { transactionDao.insertTransactions(any()) } returns listOf(1L)
        
        val result = transactionRepository.importTransactionsFromSmsWithDuplicateCheck()
        
        assertEquals(2, result.totalSmsProcessed)
        assertEquals(2, result.transactionsParsed)
        assertEquals(1, result.transactionsImported)
        assertEquals(1, result.duplicatesFound)
        assertFalse(result.hasErrors)
    }
    
    @Test
    fun `getCategorySpendingSummary returns spending by category`() = runTest {
        val startDate = Date()
        val endDate = Date()
        val categorySpending = listOf(
            com.expensetracker.data.local.dao.CategorySpending("Food & Dining", 500.0),
            com.expensetracker.data.local.dao.CategorySpending("Transportation", 200.0)
        )
        
        coEvery { transactionDao.getCategorySpendingSummary(startDate, endDate) } returns categorySpending
        
        val result = transactionRepository.getCategorySpendingSummary(startDate, endDate)
        
        assertEquals(2, result.size)
        assertEquals(500.0, result["Food & Dining"], 0.01)
        assertEquals(200.0, result["Transportation"], 0.01)
    }
    
    @Test
    fun `getTotalAmountByDateRange returns correct total`() = runTest {
        val startDate = Date()
        val endDate = Date()
        val expectedTotal = 750.0
        
        coEvery { transactionDao.getTotalAmountByDateRange(startDate, endDate) } returns expectedTotal
        
        val result = transactionRepository.getTotalAmountByDateRange(startDate, endDate)
        
        assertEquals(expectedTotal, result, 0.01)
    }
    
    @Test
    fun `getTotalAmountByDateRange returns zero when no data`() = runTest {
        val startDate = Date()
        val endDate = Date()
        
        coEvery { transactionDao.getTotalAmountByDateRange(startDate, endDate) } returns null
        
        val result = transactionRepository.getTotalAmountByDateRange(startDate, endDate)
        
        assertEquals(0.0, result, 0.01)
    }
    
    @Test
    fun `searchTransactions returns filtered transactions`() = runTest {
        val query = "ZOMATO"
        val transactionEntities = listOf(createTransactionEntity(merchantName = "ZOMATO"))
        
        every { transactionDao.searchTransactions(query) } returns flowOf(transactionEntities)
        
        val result = transactionRepository.searchTransactions(query)
        
        result.collect { transactions ->
            assertEquals(1, transactions.size)
            assertEquals("ZOMATO", transactions[0].merchantName)
        }
    }
    
    @Test
    fun `deleteTransaction removes transaction from database`() = runTest {
        val transaction = createValidTransaction()
        
        coEvery { transactionDao.deleteTransaction(any()) } just Runs
        
        transactionRepository.deleteTransaction(transaction)
        
        coVerify { transactionDao.deleteTransaction(any()) }
    }
    
    @Test
    fun `deleteTransactionById removes transaction by ID`() = runTest {
        val transactionId = 1L
        
        coEvery { transactionDao.deleteTransactionById(transactionId) } just Runs
        
        transactionRepository.deleteTransactionById(transactionId)
        
        coVerify { transactionDao.deleteTransactionById(transactionId) }
    }
    
    @Test
    fun `updateTransaction updates existing transaction`() = runTest {
        val transaction = createValidTransaction().copy(id = 1L)
        
        coEvery { transactionDao.updateTransaction(any()) } just Runs
        
        transactionRepository.updateTransaction(transaction)
        
        coVerify { transactionDao.updateTransaction(any()) }
    }
    
    @Test
    fun `getTransactionById returns transaction when found`() = runTest {
        val transactionId = 1L
        val transactionEntity = createTransactionEntity(id = transactionId)
        
        coEvery { transactionDao.getTransactionById(transactionId) } returns transactionEntity
        
        val result = transactionRepository.getTransactionById(transactionId)
        
        assertNotNull(result)
        assertEquals(transactionId, result!!.id)
    }
    
    @Test
    fun `getTransactionById returns null when not found`() = runTest {
        val transactionId = 999L
        
        coEvery { transactionDao.getTransactionById(transactionId) } returns null
        
        val result = transactionRepository.getTransactionById(transactionId)
        
        assertNull(result)
    }
    
    private fun createValidTransaction(): Transaction {
        return Transaction(
            id = 0,
            amount = 100.0,
            recipient = "9876543210",
            merchantName = "ZOMATO",
            dateTime = Date(),
            transactionId = "TXN123",
            paymentMethod = Transaction.PAYMENT_METHOD_UPI,
            category = null,
            notes = null,
            isCategorized = false,
            smsContent = "₹100 paid to ZOMATO via UPI"
        )
    }
    
    private fun createTransactionEntity(
        id: Long = 1L,
        amount: Double = 100.0,
        recipient: String? = "9876543210",
        merchantName: String? = "ZOMATO",
        isCategorized: Boolean = false
    ): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            recipient = recipient,
            merchantName = merchantName,
            dateTime = Date(),
            transactionId = "TXN123",
            paymentMethod = "UPI",
            category = null,
            notes = null,
            isCategorized = isCategorized,
            smsContent = "₹$amount paid to ${merchantName ?: recipient} via UPI"
        )
    }
    
    private fun createSmsMessage(): SmsMessage {
        return SmsMessage(
            id = 1L,
            address = "GPAY",
            body = "₹100 paid to ZOMATO via UPI",
            date = Date(),
            type = SmsMessage.TYPE_INBOX,
            read = SmsMessage.STATUS_READ
        )
    }
    
    private fun createParsedTransaction(): ParsedTransaction {
        return ParsedTransaction(
            amount = 100.0,
            recipient = null,
            merchantName = "ZOMATO",
            transactionId = "TXN123",
            paymentMethod = ParsedTransaction.METHOD_UPI,
            dateTime = Date(),
            smsContent = "₹100 paid to ZOMATO via UPI",
            sender = "GPAY"
        )
    }
}