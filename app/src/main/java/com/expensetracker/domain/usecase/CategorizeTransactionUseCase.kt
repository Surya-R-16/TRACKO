package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.domain.usecase.base.UseCase
import com.expensetracker.domain.util.CategoryValidator
import javax.inject.Inject

/**
 * Use case for categorizing transactions
 */
class CategorizeTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) : UseCase<CategorizeTransactionUseCase.Params, CategorizeTransactionResult>() {
    
    override suspend fun execute(parameters: Params): CategorizeTransactionResult {
        return try {
            // Validate category
            if (!CategoryValidator.isValidColorFormat(parameters.category)) {
                return CategorizeTransactionResult.Error("Invalid category: ${parameters.category}")
            }
            
            when (parameters.type) {
                CategorizeType.SINGLE -> {
                    transactionRepository.categorizeTransaction(
                        parameters.transactionId!!,
                        parameters.category,
                        parameters.notes
                    )
                    CategorizeTransactionResult.Success(1)
                }
                CategorizeType.MULTIPLE -> {
                    transactionRepository.categorizeMultipleTransactions(
                        parameters.transactionIds!!,
                        parameters.category
                    )
                    CategorizeTransactionResult.Success(parameters.transactionIds.size)
                }
                CategorizeType.UNCATEGORIZE -> {
                    transactionRepository.uncategorizeTransaction(parameters.transactionId!!)
                    CategorizeTransactionResult.Success(1)
                }
            }
        } catch (e: Exception) {
            CategorizeTransactionResult.Error(e.message ?: "Failed to categorize transaction")
        }
    }
    
    data class Params(
        val type: CategorizeType,
        val category: String,
        val transactionId: Long? = null,
        val transactionIds: List<Long>? = null,
        val notes: String? = null
    ) {
        companion object {
            fun single(transactionId: Long, category: String, notes: String? = null) = Params(
                type = CategorizeType.SINGLE,
                category = category,
                transactionId = transactionId,
                notes = notes
            )
            
            fun multiple(transactionIds: List<Long>, category: String) = Params(
                type = CategorizeType.MULTIPLE,
                category = category,
                transactionIds = transactionIds
            )
            
            fun uncategorize(transactionId: Long) = Params(
                type = CategorizeType.UNCATEGORIZE,
                category = "", // Empty category for uncategorization
                transactionId = transactionId
            )
        }
    }
    
    enum class CategorizeType {
        SINGLE,
        MULTIPLE,
        UNCATEGORIZE
    }
}

/**
 * Result of categorization operation
 */
sealed class CategorizeTransactionResult {
    data class Success(val transactionsAffected: Int) : CategorizeTransactionResult()
    data class Error(val message: String) : CategorizeTransactionResult()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}