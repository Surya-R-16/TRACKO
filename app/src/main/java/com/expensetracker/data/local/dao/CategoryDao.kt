package com.expensetracker.data.local.dao

import androidx.room.*
import com.expensetracker.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>): List<Long>
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE is_default = 1 ORDER BY name ASC")
    fun getDefaultCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE is_default = 0 ORDER BY name ASC")
    fun getCustomCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?
    
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
    
    @Query("SELECT COUNT(*) FROM categories WHERE is_default = 0")
    suspend fun getCustomCategoryCount(): Int
    
    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    suspend fun isCategoryNameExists(name: String): Int
    
    @Query("SELECT name FROM categories ORDER BY name ASC")
    suspend fun getAllCategoryNames(): List<String>
    
    @Query("SELECT name FROM categories WHERE is_default = 0 ORDER BY name ASC")
    suspend fun getCustomCategoryNames(): List<String>
    
    @Query("""
        SELECT c.* FROM categories c 
        LEFT JOIN transactions t ON c.name = t.category 
        WHERE c.is_default = 0 
        GROUP BY c.id 
        HAVING COUNT(t.id) = 0
        ORDER BY c.name ASC
    """)
    suspend fun getUnusedCustomCategories(): List<Category>
    
    @Query("""
        SELECT c.*, COUNT(t.id) as transaction_count 
        FROM categories c 
        LEFT JOIN transactions t ON c.name = t.category 
        GROUP BY c.id 
        ORDER BY transaction_count DESC, c.name ASC
    """)
    suspend fun getCategoriesWithTransactionCount(): List<CategoryWithCount>
    
    @Query("DELETE FROM categories WHERE is_default = 0")
    suspend fun deleteAllCustomCategories()
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}

/**
 * Data class for category with transaction count
 */
data class CategoryWithCount(
    @Embedded val category: Category,
    val transaction_count: Int
)