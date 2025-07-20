package com.expensetracker.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.expensetracker.data.local.database.AppDatabase
import com.expensetracker.data.local.entity.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        categoryDao = database.categoryDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetCategory() = runTest {
        val category = createSampleCategory()
        
        val id = categoryDao.insertCategory(category)
        assertTrue(id > 0)
        
        val retrievedCategory = categoryDao.getCategoryById(id)
        assertNotNull(retrievedCategory)
        assertEquals(category.name, retrievedCategory!!.name)
        assertEquals(category.color, retrievedCategory.color)
    }
    
    @Test
    fun getAllCategories() = runTest {
        val categories = listOf(
            createSampleCategory(name = "Food", isDefault = true),
            createSampleCategory(name = "Transportation", isDefault = true),
            createSampleCategory(name = "Custom Category", isDefault = false)
        )
        
        categoryDao.insertCategories(categories)
        
        val allCategories = categoryDao.getAllCategories().first()
        assertEquals(3, allCategories.size)
        
        // Should be ordered by name ASC
        assertEquals("Custom Category", allCategories[0].name)
        assertEquals("Food", allCategories[1].name)
        assertEquals("Transportation", allCategories[2].name)
    }
    
    @Test
    fun getDefaultCategories() = runTest {
        val categories = listOf(
            createSampleCategory(name = "Food", isDefault = true),
            createSampleCategory(name = "Custom Category", isDefault = false)
        )
        
        categoryDao.insertCategories(categories)
        
        val defaultCategories = categoryDao.getDefaultCategories().first()
        assertEquals(1, defaultCategories.size)
        assertEquals("Food", defaultCategories[0].name)
        assertTrue(defaultCategories[0].isDefault)
    }
    
    @Test
    fun getCustomCategories() = runTest {
        val categories = listOf(
            createSampleCategory(name = "Food", isDefault = true),
            createSampleCategory(name = "Custom Category", isDefault = false)
        )
        
        categoryDao.insertCategories(categories)
        
        val customCategories = categoryDao.getCustomCategories().first()
        assertEquals(1, customCategories.size)
        assertEquals("Custom Category", customCategories[0].name)
        assertFalse(customCategories[0].isDefault)
    }
    
    @Test
    fun getCategoryByName() = runTest {
        val category = createSampleCategory(name = "Test Category")
        categoryDao.insertCategory(category)
        
        val foundCategory = categoryDao.getCategoryByName("Test Category")
        assertNotNull(foundCategory)
        assertEquals("Test Category", foundCategory!!.name)
        
        val notFoundCategory = categoryDao.getCategoryByName("Non-existent Category")
        assertNull(notFoundCategory)
    }
    
    @Test
    fun isCategoryNameExists() = runTest {
        val category = createSampleCategory(name = "Existing Category")
        categoryDao.insertCategory(category)
        
        val existsCount = categoryDao.isCategoryNameExists("Existing Category")
        assertEquals(1, existsCount)
        
        val notExistsCount = categoryDao.isCategoryNameExists("Non-existent Category")
        assertEquals(0, notExistsCount)
    }
    
    @Test
    fun getAllCategoryNames() = runTest {
        val categories = listOf(
            createSampleCategory(name = "Food"),
            createSampleCategory(name = "Transportation"),
            createSampleCategory(name = "Shopping")
        )
        
        categoryDao.insertCategories(categories)
        
        val categoryNames = categoryDao.getAllCategoryNames()
        assertEquals(3, categoryNames.size)
        assertTrue(categoryNames.contains("Food"))
        assertTrue(categoryNames.contains("Transportation"))
        assertTrue(categoryNames.contains("Shopping"))
        
        // Should be ordered by name ASC
        assertEquals("Food", categoryNames[0])
        assertEquals("Shopping", categoryNames[1])
        assertEquals("Transportation", categoryNames[2])
    }
    
    @Test
    fun deleteCategory() = runTest {
        val category = createSampleCategory(name = "To Delete")
        val id = categoryDao.insertCategory(category)
        
        // Verify category exists
        val existingCategory = categoryDao.getCategoryById(id)
        assertNotNull(existingCategory)
        
        // Delete category
        categoryDao.deleteCategoryById(id)
        
        // Verify category is deleted
        val deletedCategory = categoryDao.getCategoryById(id)
        assertNull(deletedCategory)
    }
    
    @Test
    fun getCategoryCount() = runTest {
        assertEquals(0, categoryDao.getCategoryCount())
        
        val categories = listOf(
            createSampleCategory(name = "Category 1", isDefault = true),
            createSampleCategory(name = "Category 2", isDefault = false)
        )
        
        categoryDao.insertCategories(categories)
        
        assertEquals(2, categoryDao.getCategoryCount())
        assertEquals(1, categoryDao.getCustomCategoryCount())
    }
    
    @Test
    fun deleteAllCustomCategories() = runTest {
        val categories = listOf(
            createSampleCategory(name = "Default Category", isDefault = true),
            createSampleCategory(name = "Custom Category 1", isDefault = false),
            createSampleCategory(name = "Custom Category 2", isDefault = false)
        )
        
        categoryDao.insertCategories(categories)
        
        assertEquals(3, categoryDao.getCategoryCount())
        
        categoryDao.deleteAllCustomCategories()
        
        assertEquals(1, categoryDao.getCategoryCount())
        val remainingCategories = categoryDao.getAllCategories().first()
        assertTrue(remainingCategories.all { it.isDefault })
    }
    
    private fun createSampleCategory(
        name: String = "Test Category",
        color: String = "#FF5722",
        icon: String? = "test_icon",
        isDefault: Boolean = false
    ): Category {
        return Category(
            name = name,
            color = color,
            icon = icon,
            isDefault = isDefault,
            createdAt = Date()
        )
    }
}