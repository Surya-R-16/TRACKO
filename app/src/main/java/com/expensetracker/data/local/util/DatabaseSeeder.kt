package com.expensetracker.data.local.util

import com.expensetracker.data.local.dao.CategoryDao
import com.expensetracker.data.mapper.toEntity
import com.expensetracker.domain.util.DefaultCategories
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for seeding the database with default data
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val categoryDao: CategoryDao
) {
    
    /**
     * Seeds the database with default categories if they don't exist
     */
    suspend fun seedDefaultCategories() {
        val existingCategoryCount = categoryDao.getCategoryCount()
        
        if (existingCategoryCount == 0) {
            val defaultCategories = DefaultCategories.getDefaultCategories()
            categoryDao.insertCategories(defaultCategories.toEntity())
        }
    }
    
    /**
     * Checks if default categories need to be re-seeded (in case some were deleted)
     */
    suspend fun ensureDefaultCategoriesExist() {
        val defaultCategories = DefaultCategories.getDefaultCategories()
        
        for (defaultCategory in defaultCategories) {
            val existingCategory = categoryDao.getCategoryByName(defaultCategory.name)
            if (existingCategory == null) {
                categoryDao.insertCategory(defaultCategory.toEntity())
            }
        }
    }
    
    /**
     * Resets all categories to default state (for testing or reset functionality)
     */
    suspend fun resetToDefaultCategories() {
        // Delete all custom categories
        categoryDao.deleteAllCustomCategories()
        
        // Ensure all default categories exist
        ensureDefaultCategoriesExist()
    }
}