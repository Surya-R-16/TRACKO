package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CategorizeTransactionUseCaseTest {
    
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categorizeTransactionUseCase: CategorizeTransactionUseCase
    
    @Before
    fun setup() {
        transactionRepository = mockk()
        categorizeTransactionUseCase = CategorizeTransactionUseCase(transactionRepository)
    }
    
    @Test
    fun `invoke with SINGLE categorization succeeds`() = runTest {
        val transactionId = 1L
        val category = "Food & Dining"
        val notes = "Lunch"
        
        coEvery { transactionRepository.categorizeTransaction(transactionId, category, notes) } just Runs
        
        val params = CategorizeTransactionUseCase.Params.single(transactionId, category, notes)
        val result = categorizeTransactionUseCase(params)
        
        assertTrue(result.isSuccess)
        assertTrue(result is CategorizeTransactionResult.Success)
        assertEquals(1, (result as CategorizeTransactionResult.Success).transactionsAffected)
        
        coVerify { transactionRepository.categorizeTransaction(transactionId, category, notes) }
    }
    
    @Test
    fun `invoke with MULTIPLE categorization succeeds`() = runTest {
        val transactionIds = listOf(1L, 2L, 3L)
        val category = "Transportation"
        
        coEvery { transactionRepository.categorizeMultipleTransactions(transactionIds, category) } just Runs
        
        val params = CategorizeTransactionUseCase.Params.multiple(transactionIds, category)
        val result = categorizeTransactionUseCase(params)
        
        assertTrue(result.isSuccess)
        assertTrue(result is CategorizeTransactionResult.Success)
        assertEquals(3, (result as CategorizeTransactionResult.Success).transactionsAffected)
        
        coVerify { transactionRepository.categorizeMultipleTransactions(transactionIds, category) }
    }
    
    @Test
    fun `invoke with UNCATEGORIZE succeeds`() = runTest {
        val transactionId = 1L
        
        coEvery { transactionRepository.uncategorizeTransaction(transactionId) } just Runs
        
        val params = CategorizeTransactionUseCase.Params.uncategorize(transactionId)
        val result = categorizeTransactionUseCase(params)
        
        assertTrue(result.isSuccess)
        assertTrue(result is CategorizeTransactionResult.Success)
        assertEquals(1, (result as CategorizeTransactionResult.Success).transactionsAffected)
        
        coVerify { transactionRepository.uncategorizeTransaction(transactionId) }
    }
    
    @Test
    fun `invoke handles repository exception`() = runTest {
        val transactionId = 1L
        val category = "Food & Dining"
        val errorMessage = "Database error"
        
        coEvery { 
            transactionRepository.categorizeTransaction(transactionId, category, null) 
        } throws RuntimeException(errorMessage)
        
        val params = CategorizeTransactionUseCase.Params.single(transactionId, category)
        val result = categorizeTransactionUseCase(params)
        
        assertTrue(result.isError)
        assertTrue(result is CategorizeTransactionResult.Error)
        assertEquals(errorMessage, (result as CategorizeTransactionResult.Error).message)
    }
    
    @Test
    fun `Params companion object creates correct parameters`() {
        // Test single categorization params
        val singleParams = CategorizeTransactionUseCase.Params.single(1L, "Food", "Notes")
        assertEquals(CategorizeTransactionUseCase.CategorizeType.SINGLE, singleParams.type)
        assertEquals(1L, singleParams.transactionId)
        assertEquals("Food", singleParams.category)
        assertEquals("Notes", singleParams.notes)
        
        // Test multiple categorization params
        val multipleParams = CategorizeTransactionUseCase.Params.multiple(listOf(1L, 2L), "Transport")
        assertEquals(CategorizeTransactionUseCase.CategorizeType.MULTIPLE, multipleParams.type)
        assertEquals(listOf(1L, 2L), multipleParams.transactionIds)
        assertEquals("Transport", multipleParams.category)
        
        // Test uncategorize params
        val uncategorizeParams = CategorizeTransactionUseCase.Params.uncategorize(1L)
        assertEquals(CategorizeTransactionUseCase.CategorizeType.UNCATEGORIZE, uncategorizeParams.type)
        assertEquals(1L, uncategorizeParams.transactionId)
        assertEquals("", uncategorizeParams.category)
    }
    
    @Test
    fun `CategorizeTransactionResult properties work correctly`() {
        val successResult = CategorizeTransactionResult.Success(5)
        assertTrue(successResult.isSuccess)
        assertFalse(successResult.isError)
        
        val errorResult = CategorizeTransactionResult.Error("Error message")
        assertFalse(errorResult.isSuccess)
        assertTrue(errorResult.isError)
    }
}