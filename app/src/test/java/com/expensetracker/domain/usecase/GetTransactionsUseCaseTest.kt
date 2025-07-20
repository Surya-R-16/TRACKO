package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.domain.model.Transaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class GetTransactionsUseCaseTest {
    
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    
    @Before
    fun setup() {
        transactionRepository = mockk()
        getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    }
    
    @Test
    fun `invoke with ALL filter returns all transactions`() = runTest {
        val transactions = listOf(createTransaction())
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)
        
        val params = GetTransactionsUseCase.Params.all()
        val result = getTransactionsUseCase(params).toList()
        
        assertEquals(1, result.size)
        assertEquals(transactions, result[0])
        verify { transactionRepository.getAllTransactions() }
    }
    
    @Test
    fun `invoke with UNCATEGORIZED filter returns uncategorized transactions`() = runTest {
        val transactions = listOf(createTransaction(isCategorized = false))
        every { transactionRepository.getUncategorizedTransactions() } returns flowOf(transactions)
        
        val params = GetTransactionsUseCase.Params.uncategorized()
        val result = getTransactionsUseCase(params).toList()
        
        assertEquals(1, result.size)
        assertEquals(transactions, result[0])
        verify { transactionRepository.getUncategorizedTransactions() }
    }
    
    @Test
    fun `invoke with CATEGORIZED filter returns categorized transactions`() = runTest {
        val transactions = listOf(createTransaction(isCategorized = true))
        every { transactionRepository.getCategorizedTransactions() } returns flowOf(transactions)
        
        val params = GetTransactionsUseCase.Params.categorized()
        val result = getTransactionsUseCase(params).toList()
        
        assertEquals(1, result.size)
        assertEquals(transactions, result[0])
        verify { transactionRepository.getCategorizedTransactions() }
    }
    
    @Test
    fun `invoke with BY_CATEGORY filter returns transactions by category`() = runTest {
        val category = "Food & Dining"
        val transactions = listOf(createTransaction(category = category))
        every { transactionRepository.getTransactionsByCategory(category) } returns flowOf(transactions)
        
        val params = GetTransactionsUseCase.Params.byCategory(category)
        val result = getTransactionsUseCase(params).toList()
        
        assertEquals(1, result.size)
        assertEquals(transactions, result[0])
        verify { transactionRepository.getTransactionsByCategory(category) }
    }
    
    @Test
    fun `invoke with BY_DATE_RANGE filter returns transactions by date range`() = runTest {
        val startDate = Date()
        val endDate = Date()
        val transactions = listOf(createTransaction())
        every { transactionRepository.getTransactionsByDateRange(startDate, endDate) } returns flowOf(transactions)
        
        val params = GetTransactionsUseCase.Params.byDateRange(startDate, endDate)
        val result = getTransactionsUseCase(params).toList()
        
        assertEquals(1, result.size)
        assertEquals(transactions, result[0])
        verify { transactionRepository.getTransactionsByDateRange(startDate, endDate) }
    }
    
    @Test
    fun `invoke with BY_CATEGORY_AND_DATE filter returns transactions by category and date`() = runTest {
        val category = "Transportation"
        val startDate = Date()
        val endDate = Date()
        val transactions = listOf(createTransaction(category = category))
        every { 
            transactionRepository.getTransactionsByCategoryAndDateRange(category, startDate, endDate) 
        } returns flowOf(transactions)
        
        val params = GetTransactionsUseCase.Params.byCategoryAndDate(category, startDate, endDate)
        val result = getTransactionsUseCase(params).toList()
        
        assertEquals(1, result.size)
        assertEquals(transactions, result[0])
        verify { transactionRepository.getTransactionsByCategoryAndDateRange(category, startDate, endDate) }
    }
    
    @Test
    fun `invoke with SEARCH filter returns search results`() = runTest {
        val query = "ZOMATO"
        val transactions = listOf(createTransaction(merchantName = "ZOMATO"))
        every { transactionRepository.searchTransactions(query) } returns flowOf(transactions)
        
        val params = GetTransactionsUseCase.Params.search(query)
        val result = getTransactionsUseCase(params).toList()
        
        assertEquals(1, result.size)
        assertEquals(transactions, result[0])
        verify { transactionRepository.searchTransactions(query) }
    }
    
    private fun createTransaction(
        id: Long = 1L,
        amount: Double = 100.0,
        merchantName: String = "TEST_MERCHANT",
        category: String? = null,
        isCategorized: Boolean = false
    ): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            recipient = null,
            merchantName = merchantName,
            dateTime = Date(),
            transactionId = "TXN123",
            paymentMethod = Transaction.PAYMENT_METHOD_UPI,
            category = category,
            notes = null,
            isCategorized = isCategorized,
            smsContent = "Test SMS content"
        )
    }
}