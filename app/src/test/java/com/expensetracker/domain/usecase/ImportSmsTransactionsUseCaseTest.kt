package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.data.repository.model.ImportResult
import com.expensetracker.data.repository.model.ImportError
import com.expensetracker.data.repository.model.ImportErrorType
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ImportSmsTransactionsUseCaseTest {
    
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var importSmsTransactionsUseCase: ImportSmsTransactionsUseCase
    
    @Before
    fun setup() {
        transactionRepository = mockk()
        importSmsTransactionsUseCase = ImportSmsTransactionsUseCase(transactionRepository)
    }
    
    @Test
    fun `invoke with duplicate check enabled calls correct repository method`() = runTest {
        val importResult = createSuccessfulImportResult()
        coEvery { transactionRepository.importTransactionsFromSmsWithDuplicateCheck() } returns importResult
        
        val params = ImportSmsTransactionsUseCase.Params.withDuplicateCheck()
        val result = importSmsTransactionsUseCase(params)
        
        assertTrue(result.isSuccess)
        assertTrue(result is ImportSmsTransactionsResult.Success)
        assertEquals(importResult, (result as ImportSmsTransactionsResult.Success).importResult)
        
        coVerify { transactionRepository.importTransactionsFromSmsWithDuplicateCheck() }
        coVerify(exactly = 0) { transactionRepository.importTransactionsFromSms() }
    }
    
    @Test
    fun `invoke with duplicate check disabled calls correct repository method`() = runTest {
        val importResult = createSuccessfulImportResult()
        coEvery { transactionRepository.importTransactionsFromSms() } returns importResult
        
        val params = ImportSmsTransactionsUseCase.Params.withoutDuplicateCheck()
        val result = importSmsTransactionsUseCase(params)
        
        assertTrue(result.isSuccess)
        assertTrue(result is ImportSmsTransactionsResult.Success)
        assertEquals(importResult, (result as ImportSmsTransactionsResult.Success).importResult)
        
        coVerify { transactionRepository.importTransactionsFromSms() }
        coVerify(exactly = 0) { transactionRepository.importTransactionsFromSmsWithDuplicateCheck() }
    }
    
    @Test
    fun `invoke handles repository exception`() = runTest {
        val errorMessage = "SMS permission denied"
        coEvery { transactionRepository.importTransactionsFromSmsWithDuplicateCheck() } throws RuntimeException(errorMessage)
        
        val params = ImportSmsTransactionsUseCase.Params.withDuplicateCheck()
        val result = importSmsTransactionsUseCase(params)
        
        assertTrue(result.isError)
        assertTrue(result is ImportSmsTransactionsResult.Error)
        assertEquals(errorMessage, (result as ImportSmsTransactionsResult.Error).message)
    }
    
    @Test
    fun `invoke returns import result with errors when repository returns errors`() = runTest {
        val importResult = createImportResultWithErrors()
        coEvery { transactionRepository.importTransactionsFromSmsWithDuplicateCheck() } returns importResult
        
        val params = ImportSmsTransactionsUseCase.Params.withDuplicateCheck()
        val result = importSmsTransactionsUseCase(params)
        
        assertTrue(result.isSuccess) // Use case succeeds even if import has errors
        assertTrue(result is ImportSmsTransactionsResult.Success)
        
        val returnedImportResult = (result as ImportSmsTransactionsResult.Success).importResult
        assertTrue(returnedImportResult.hasErrors)
        assertEquals(1, returnedImportResult.errors.size)
        assertEquals(ImportErrorType.PERMISSION_DENIED, returnedImportResult.errors[0].type)
    }
    
    @Test
    fun `Params companion object creates correct parameters`() {
        val withDuplicateCheck = ImportSmsTransactionsUseCase.Params.withDuplicateCheck()
        assertTrue(withDuplicateCheck.enableDuplicateCheck)
        
        val withoutDuplicateCheck = ImportSmsTransactionsUseCase.Params.withoutDuplicateCheck()
        assertFalse(withoutDuplicateCheck.enableDuplicateCheck)
    }
    
    @Test
    fun `ImportSmsTransactionsResult properties work correctly`() {
        val importResult = createSuccessfulImportResult()
        val successResult = ImportSmsTransactionsResult.Success(importResult)
        assertTrue(successResult.isSuccess)
        assertFalse(successResult.isError)
        assertEquals(importResult, successResult.getImportResult())
        
        val errorResult = ImportSmsTransactionsResult.Error("Error message")
        assertFalse(errorResult.isSuccess)
        assertTrue(errorResult.isError)
        assertNull(errorResult.getImportResult())
    }
    
    private fun createSuccessfulImportResult(): ImportResult {
        return ImportResult(
            totalSmsProcessed = 10,
            transactionsParsed = 8,
            transactionsImported = 7,
            duplicatesFound = 1,
            errors = emptyList(),
            importedTransactions = emptyList(),
            duplicateTransactions = emptyList()
        )
    }
    
    private fun createImportResultWithErrors(): ImportResult {
        return ImportResult(
            totalSmsProcessed = 0,
            transactionsParsed = 0,
            transactionsImported = 0,
            duplicatesFound = 0,
            errors = listOf(
                ImportError(ImportErrorType.PERMISSION_DENIED, "SMS permission not granted")
            ),
            importedTransactions = emptyList(),
            duplicateTransactions = emptyList()
        )
    }
}