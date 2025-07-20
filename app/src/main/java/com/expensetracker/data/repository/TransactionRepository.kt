package com.expensetracker.data.repository

import com.expensetracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for transaction data operations
 */
interface TransactionRepository {
    
    // Basic CRUD operations
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun insertTransactions(transactions: List<Transaction>): List<Long>
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun deleteTransactionById(id: Long)
    
    // Query operations
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getUncategorizedTransactions(): Flow<List<Transaction>>
    fun getCategorizedTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    
    // Advanced query operations
    fun getTransactionsByCategoryAndDateRange(
        category: String, 
        startDate: Date, 
        endDate: Date
    ): Flow<List<Transaction>>
    
    fun getUncategorizedTransactionCount(): Flow<Int>
    suspend fun getTotalTransactionCount(): Int
    suspend fun getTotalAmountByDateRange(startDate: Date, endDate: Date): Double
    suspend fun getTotalAmountByCategoryAndDateRange(
        category: String, 
        startDate: Date, 
        endDate: Date
    ): Double
    
    // Categorization operations
    suspend fun categorizeTransaction(id: Long, category: String, notes: String? = null)
    suspend fun categorizeMultipleTransactions(ids: List<Long>, category: String)
    suspend fun uncategorizeTransaction(id: Long)
    
    // SMS import operations
    suspend fun importTransactionsFromSms(): ImportResult
    suspend fun importTransactionsFromSmsWithDuplicateCheck(): ImportResult
    
    // Analytics operations
    suspend fun getCategorySpendingSummary(startDate: Date, endDate: Date): Map<String, Double>
    suspend fun getMonthlySpendingSummary(year: Int, month: Int): MonthlySpendingSummary
    suspend fun getSpendingTrends(startDate: Date, endDate: Date, intervalDays: Int): List<SpendingTrendData>
    
    // Duplicate detection
    suspend fun checkForDuplicates(transaction: Transaction): List<Transaction>
    suspend fun isDuplicateTransaction(transaction: Transaction): Boolean
    
    // Utility operations
    suspend fun deleteAllTransactions()
    suspend fun getTransactionStats(): TransactionStats
}