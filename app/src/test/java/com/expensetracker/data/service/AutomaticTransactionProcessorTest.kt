package com.expensetracker.data.service

import com.expensetracker.data.permission.PermissionManager
import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.data.service.notification.NotificationHelper
import com.expensetracker.data.sms.SmsContentProvider
import com.expensetracker.data.sms.SmsParserService
import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.data.sms.model.SmsMessage
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.usecase.GetTransactionCountsUseCase
import com.expensetracker.domain.usecase.TransactionCounts
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class AutomaticTransactionProcessorTest {
    
    private lateinit var smsContentProvider: SmsContentProvider
    private lateinit var smsParserService: SmsParserService
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var permissionManager: PermissionManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var getTransactionCountsUseCase: GetTransactionCountsUseCase
    private lateinit var automaticTransactionProcessor: AutomaticTransactionProcessor
    
    @Before
    fun setup() {
        smsContentProvider = mockk()
        smsParserService = mockk()
        transactionRepository = mockk()
        permissionManager = mockk()
        notificationHelper = mockk()
        getTransactionCountsUseCase = mockk()
        
        automaticTransactionProcessor = AutomaticTransactionProcessor(
            smsContentProvider,
            smsParserService,
            transactionRepository,
            permissionManager,
            notificationHelper,
            getTransactionCountsUseCase
        )
    }
    
    @Test
    fun `processNewSmsMessages returns PermissionDenied when SMS permission not granted`() = runTest {
        every { permissionManager.hasReadSmsPermission() } returns false
        
        val result = automaticTransactionProcessor.processNewSmsMessages(0L)
        
        assertEquals(ProcessingResult.PermissionDenied, result)
        verify { permissionManager.hasReadSmsPermission() }
    }
    
    @Test
    fun `processNewSmsMessages returns NoNewMessages when no SMS found`() = runTest {
        every { permissionManager.hasReadSmsPermission() } returns true
        coEvery { smsContentProvider.getSmsMessagesByDateRange(any(), any()) } returns emptyList()
        
        val result = automaticTransactionProcessor.processNewSmsMessages(0L)
        
        assertEquals(ProcessingResult.NoNewMessages, result)
    }
    
    @Test
    fun `processNewSmsMessages returns NoTransactionMessages when no transaction SMS found`() = runTest {
        val smsMessages = listOf(createSmsMessage(body = "Your OTP is 123456"))
        
        every { permissionManager.hasReadSmsPermission() } returns true
        coEvery { smsContentProvider.getSmsMessagesByDateRange(any(), any()) } returns smsMessages
        
        val result = automaticTransactionProcessor.processNewSmsMessages(0L)
        
        assertTrue(result is ProcessingResult.NoTransactionMessages)
        assertEquals(1, (result as ProcessingResult.NoTransactionMessages).totalSmsProcessed)
    }
    
    @Test
    fun `processNewSmsMessages successfully processes valid transaction SMS`() = runTest {
        val smsMessages = listOf(createSmsMessage(body = "₹100 paid to ZOMATO via UPI"))
        val parsedTransactions = listOf(createParsedTransaction())
        
        every { permissionManager.hasReadSmsPermission() } returns true
        coEvery { smsContentProvider.getSmsMessagesByDateRange(any(), any()) } returns smsMessages
        coEvery { smsParserService.parseTransactionSmsMessages(any()) } returns parsedTransactions
        coEvery { transactionRepository.isDuplicateTransaction(any()) } returns false
        coEvery { transactionRepository.insertTransactions(any()) } returns listOf(1L)
        
        val result = automaticTransactionProcessor.processNewSmsMessages(0L)
        
        assertTrue(result.isSuccess)
        assertTrue(result.hasNewTransactions)
        assertTrue(result is ProcessingResult.Success)
        
        val successResult = result as ProcessingResult.Success
        assertEquals(1, successResult.totalSmsProcessed)
        assertEquals(1, successResult.transactionSmsFound)
        assertEquals(1, successResult.transactionsParsed)
        assertEquals(1, successResult.uniqueTransactions)
        assertEquals(0, successResult.duplicateTransactions)
        assertEquals(1, successResult.transactionsInserted)
    }
    
    @Test
    fun `processNewSmsMessages handles duplicate transactions correctly`() = runTest {
        val smsMessages = listOf(createSmsMessage(body = "₹100 paid to ZOMATO via UPI"))
        val parsedTransactions = listOf(createParsedTransaction())
        
        every { permissionManager.hasReadSmsPermission() } returns true
        coEvery { smsContentProvider.getSmsMessagesByDateRange(any(), any()) } returns smsMessages
        coEvery { smsParserService.parseTransactionSmsMessages(any()) } returns parsedTransactions
        coEvery { transactionRepository.isDuplicateTransaction(any()) } returns true
        
        val result = automaticTransactionProcessor.processNewSmsMessages(0L)
        
        assertTrue(result.isSuccess)
        assertFalse(result.hasNewTransactions)
        assertTrue(result is ProcessingResult.Success)
        
        val successResult = result as ProcessingResult.Success
        assertEquals(0, successResult.uniqueTransactions)
        assertEquals(1, successResult.duplicateTransactions)
        assertEquals(0, successResult.transactionsInserted)
        
        // Verify no insertion was attempted
        coVerify(exactly = 0) { transactionRepository.insertTransactions(any()) }
    }
    
    @Test
    fun `processNewSmsMessages returns Error when exception occurs`() = runTest {
        val errorMessage = "Database connection failed"
        
        every { permissionManager.hasReadSmsPermission() } returns true
        coEvery { smsContentProvider.getSmsMessagesByDateRange(any(), any()) } throws RuntimeException(errorMessage)
        
        val result = automaticTransactionProcessor.processNewSmsMessages(0L)
        
        assertTrue(result is ProcessingResult.Error)
        assertEquals(errorMessage, (result as ProcessingResult.Error).message)
    }
    
    @Test
    fun `processSingleSmsMessage returns NotTransactionSms for non-transaction SMS`() = runTest {
        val smsMessage = createSmsMessage(body = "Your OTP is 123456")
        
        val result = automaticTransactionProcessor.processSingleSmsMessage(smsMessage)
        
        assertEquals(SingleMessageResult.NotTransactionSms, result)
    }
    
    @Test
    fun `processSingleSmsMessage returns ParsingFailed when parsing fails`() = runTest {
        val smsMessage = createSmsMessage(body = "₹100 paid to ZOMATO via UPI")
        
        coEvery { smsParserService.parseTransactionSms(smsMessage) } returns null
        
        val result = automaticTransactionProcessor.processSingleSmsMessage(smsMessage)
        
        assertEquals(SingleMessageResult.ParsingFailed, result)
    }
    
    @Test
    fun `processSingleSmsMessage returns Duplicate for duplicate transaction`() = runTest {
        val smsMessage = createSmsMessage(body = "₹100 paid to ZOMATO via UPI")
        val parsedTransaction = createParsedTransaction()
        
        coEvery { smsParserService.parseTransactionSms(smsMessage) } returns parsedTransaction
        coEvery { transactionRepository.isDuplicateTransaction(any()) } returns true
        
        val result = automaticTransactionProcessor.processSingleSmsMessage(smsMessage)
        
        assertEquals(SingleMessageResult.Duplicate, result)
    }
    
    @Test
    fun `processSingleSmsMessage returns Success for valid unique transaction`() = runTest {
        val smsMessage = createSmsMessage(body = "₹100 paid to ZOMATO via UPI")
        val parsedTransaction = createParsedTransaction()
        val insertedId = 1L
        
        coEvery { smsParserService.parseTransactionSms(smsMessage) } returns parsedTransaction
        coEvery { transactionRepository.isDuplicateTransaction(any()) } returns false
        coEvery { transactionRepository.insertTransaction(any()) } returns insertedId
        
        val result = automaticTransactionProcessor.processSingleSmsMessage(smsMessage)
        
        assertTrue(result.isSuccess)
        assertTrue(result is SingleMessageResult.Success)
        assertEquals(insertedId, (result as SingleMessageResult.Success).transactionId)
    }
    
    @Test
    fun `notifyUncategorizedTransactions sends notification when uncategorized transactions exist`() = runTest {
        val transactionCounts = TransactionCounts(
            total = 10,
            categorized = 7,
            uncategorized = 3,
            categorizationRate = 70f
        )
        
        coEvery { getTransactionCountsUseCase.getTransactionCounts() } returns transactionCounts
        every { notificationHelper.showUncategorizedTransactionsNotification(3) } just Runs
        
        automaticTransactionProcessor.notifyUncategorizedTransactions()
        
        verify { notificationHelper.showUncategorizedTransactionsNotification(3) }
    }
    
    @Test
    fun `notifyUncategorizedTransactions does not send notification when no uncategorized transactions`() = runTest {
        val transactionCounts = TransactionCounts(
            total = 10,
            categorized = 10,
            uncategorized = 0,
            categorizationRate = 100f
        )
        
        coEvery { getTransactionCountsUseCase.getTransactionCounts() } returns transactionCounts
        
        automaticTransactionProcessor.notifyUncategorizedTransactions()
        
        verify(exactly = 0) { notificationHelper.showUncategorizedTransactionsNotification(any()) }
    }
    
    @Test
    fun `processAndNotify sends appropriate notifications for successful processing`() = runTest {
        val smsMessages = listOf(createSmsMessage(body = "₹100 paid to ZOMATO via UPI"))
        val parsedTransactions = listOf(createParsedTransaction())
        val transactionCounts = TransactionCounts(10, 7, 3, 70f)
        
        every { permissionManager.hasReadSmsPermission() } returns true
        coEvery { smsContentProvider.getSmsMessagesByDateRange(any(), any()) } returns smsMessages
        coEvery { smsParserService.parseTransactionSmsMessages(any()) } returns parsedTransactions
        coEvery { transactionRepository.isDuplicateTransaction(any()) } returns false
        coEvery { transactionRepository.insertTransactions(any()) } returns listOf(1L)
        coEvery { getTransactionCountsUseCase.getTransactionCounts() } returns transactionCounts
        every { notificationHelper.showNewTransactionsNotification(1, 0) } just Runs
        every { notificationHelper.showUncategorizedTransactionsNotification(3) } just Runs
        
        val result = automaticTransactionProcessor.processAndNotify(0L)
        
        assertTrue(result.isSuccess)
        verify { notificationHelper.showNewTransactionsNotification(1, 0) }
        verify { notificationHelper.showUncategorizedTransactionsNotification(3) }
    }
    
    @Test
    fun `getProcessingStatistics returns correct statistics`() = runTest {
        val transactionCounts = TransactionCounts(15, 10, 5, 66.7f)
        
        coEvery { getTransactionCountsUseCase.getTransactionCounts() } returns transactionCounts
        coEvery { transactionRepository.getTotalTransactionCount() } returns 15
        
        val stats = automaticTransactionProcessor.getProcessingStatistics()
        
        assertEquals(15, stats.totalTransactions)
        assertEquals(10, stats.categorizedTransactions)
        assertEquals(5, stats.uncategorizedTransactions)
        assertEquals(66.7f, stats.categorizationRate, 0.1f)
        assertTrue(stats.hasTransactions)
        assertTrue(stats.hasUncategorizedTransactions)
    }
    
    private fun createSmsMessage(
        id: Long = 1L,
        address: String = "GPAY",
        body: String = "Test SMS",
        date: Date = Date(),
        type: Int = SmsMessage.TYPE_INBOX,
        read: Int = SmsMessage.STATUS_READ
    ): SmsMessage {
        return SmsMessage(id, address, body, date, type, read)
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