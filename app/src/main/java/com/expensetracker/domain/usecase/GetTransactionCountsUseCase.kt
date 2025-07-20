package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.domain.usecase.base.FlowUseCase
import com.expensetracker.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting transaction counts and statistics
 */
class GetTransactionCountsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    
    /**
     * Gets uncategorized transaction count as Flow
     */
    fun getUncategorizedCount(): Flow<Int> {
        return transactionRepository.getUncategorizedTransactionCount()
    }
    
    /**
     * Gets total transaction count
     */
    suspend fun getTotalCount(): Int {
        return transactionRepository.getTotalTransactionCount()
    }
    
    /**
     * Gets comprehensive transaction counts
     */
    suspend fun getTransactionCounts(): TransactionCounts {
        return try {
            val totalCount = transactionRepository.getTotalTransactionCount()
            val stats = transactionRepository.getTransactionStats()
            
            TransactionCounts(
                total = totalCount,
                categorized = stats.categorizedTransactions,
                uncategorized = stats.uncategorizedTransactions,
                categorizationRate = stats.categorizationRate
            )
        } catch (e: Exception) {
            TransactionCounts(
                total = 0,
                categorized = 0,
                uncategorized = 0,
                categorizationRate = 0f
            )
        }
    }
}

/**
 * Data class representing transaction counts
 */
data class TransactionCounts(
    val total: Int,
    val categorized: Int,
    val uncategorized: Int,
    val categorizationRate: Float
) {
    val hasTransactions: Boolean get() = total > 0
    val hasUncategorizedTransactions: Boolean get() = uncategorized > 0
    val isFullyCategorized: Boolean get() = uncategorized == 0 && total > 0
}