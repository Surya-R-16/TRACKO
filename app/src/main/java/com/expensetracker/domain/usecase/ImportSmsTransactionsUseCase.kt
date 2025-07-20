package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.data.repository.model.ImportResult
import com.expensetracker.domain.usecase.base.UseCase
import javax.inject.Inject

/**
 * Use case for importing transactions from SMS messages
 */
class ImportSmsTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) : UseCase<ImportSmsTransactionsUseCase.Params, ImportSmsTransactionsResult>() {
    
    override suspend fun execute(parameters: Params): ImportSmsTransactionsResult {
        return try {
            val importResult = when (parameters.enableDuplicateCheck) {
                true -> transactionRepository.importTransactionsFromSmsWithDuplicateCheck()
                false -> transactionRepository.importTransactionsFromSms()
            }
            
            ImportSmsTransactionsResult.Success(importResult)
        } catch (e: Exception) {
            ImportSmsTransactionsResult.Error(e.message ?: "Failed to import SMS transactions")
        }
    }
    
    data class Params(
        val enableDuplicateCheck: Boolean = true
    ) {
        companion object {
            fun withDuplicateCheck() = Params(enableDuplicateCheck = true)
            fun withoutDuplicateCheck() = Params(enableDuplicateCheck = false)
        }
    }
}

/**
 * Result of SMS import operation
 */
sealed class ImportSmsTransactionsResult {
    data class Success(val importResult: ImportResult) : ImportSmsTransactionsResult()
    data class Error(val message: String) : ImportSmsTransactionsResult()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getImportResult(): ImportResult? = when (this) {
        is Success -> importResult
        is Error -> null
    }
}