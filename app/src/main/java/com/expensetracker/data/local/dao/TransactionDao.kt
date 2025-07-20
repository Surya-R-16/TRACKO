package com.expensetracker.data.local.dao

import androidx.room.*
import com.expensetracker.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>): List<Long>
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
    
    @Query("SELECT * FROM transactions ORDER BY date_time DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_categorized = 0 ORDER BY date_time DESC")
    fun getUncategorizedTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE is_categorized = 1 ORDER BY date_time DESC")
    fun getCategorizedTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date_time DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE date_time BETWEEN :startDate AND :endDate 
        ORDER BY date_time DESC
    """)
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE (recipient LIKE '%' || :query || '%' 
        OR merchant_name LIKE '%' || :query || '%' 
        OR notes LIKE '%' || :query || '%'
        OR sms_content LIKE '%' || :query || '%')
        ORDER BY date_time DESC
    """)
    fun searchTransactions(query: String): Flow<List<Transaction>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE category = :category 
        AND date_time BETWEEN :startDate AND :endDate 
        ORDER BY date_time DESC
    """)
    fun getTransactionsByCategoryAndDateRange(
        category: String, 
        startDate: Date, 
        endDate: Date
    ): Flow<List<Transaction>>
    
    @Query("SELECT COUNT(*) FROM transactions WHERE is_categorized = 0")
    fun getUncategorizedTransactionCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTotalTransactionCount(): Int
    
    @Query("SELECT SUM(amount) FROM transactions WHERE date_time BETWEEN :startDate AND :endDate")
    suspend fun getTotalAmountByDateRange(startDate: Date, endDate: Date): Double?
    
    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE category = :category 
        AND date_time BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalAmountByCategoryAndDateRange(
        category: String, 
        startDate: Date, 
        endDate: Date
    ): Double?
    
    @Query("""
        SELECT category, SUM(amount) as total_amount 
        FROM transactions 
        WHERE is_categorized = 1 
        AND date_time BETWEEN :startDate AND :endDate 
        GROUP BY category 
        ORDER BY total_amount DESC
    """)
    suspend fun getCategorySpendingSummary(startDate: Date, endDate: Date): List<CategorySpending>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE amount = :amount 
        AND (recipient = :recipient OR merchant_name = :merchantName)
        AND ABS(date_time - :dateTime) <= :timeWindowMs
        LIMIT 1
    """)
    suspend fun findPotentialDuplicate(
        amount: Double,
        recipient: String?,
        merchantName: String?,
        dateTime: Long,
        timeWindowMs: Long = 300000 // 5 minutes in milliseconds
    ): Transaction?
    
    @Query("UPDATE transactions SET category = :category, notes = :notes, is_categorized = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTransactionCategory(id: Long, category: String, notes: String?, updatedAt: Date)
    
    @Query("UPDATE transactions SET category = :category, is_categorized = 1, updated_at = :updatedAt WHERE id IN (:ids)")
    suspend fun updateMultipleTransactionCategories(ids: List<Long>, category: String, updatedAt: Date)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

/**
 * Data class for category spending summary
 */
data class CategorySpending(
    val category: String,
    val total_amount: Double
)