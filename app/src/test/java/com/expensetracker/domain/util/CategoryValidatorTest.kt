package com.expensetracker.domain.util

import com.expensetracker.domain.model.Category
import org.junit.Assert.*
import org.junit.Test

class CategoryValidatorTest {
    
    @Test
    fun `validateCategory returns valid for correct category`() {
        val category = createValidCategory()
        val result = CategoryValidator.validateCategory(category)
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `validateCategory returns invalid for empty name`() {
        val category = createValidCategory().copy(name = "")
        val result = CategoryValidator.validateCategory(category)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Category name cannot be empty"))
    }
    
    @Test
    fun `validateCategory returns invalid for blank name`() {
        val category = createValidCategory().copy(name = "   ")
        val result = CategoryValidator.validateCategory(category)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Category name cannot be empty"))
    }
    
    @Test
    fun `validateCategory returns invalid for too long name`() {
        val longName = "a".repeat(51) // 51 characters
        val category = createValidCategory().copy(name = longName)
        val result = CategoryValidator.validateCategory(category)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("too long") })
    }
    
    @Test
    fun `validateCategory returns invalid for invalid color format`() {
        val category = createValidCategory().copy(color = "invalid-color")
        val result = CategoryValidator.validateCategory(category)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Invalid color format. Use hex format like #FF5722"))
    }
    
    @Test
    fun `validateCategoryName returns valid for unique name`() {
        val result = CategoryValidator.validateCategoryName("New Category", listOf("Existing Category"))
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `validateCategoryName returns invalid for duplicate name`() {
        val existingNames = listOf("Food", "Transportation", "Shopping")
        val result = CategoryValidator.validateCategoryName("Food", existingNames)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Category name already exists"))
    }
    
    @Test
    fun `validateCategoryName returns invalid for empty name`() {
        val result = CategoryValidator.validateCategoryName("", emptyList())
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Category name cannot be empty"))
    }
    
    @Test
    fun `isValidColorFormat returns true for valid hex colors`() {
        assertTrue(CategoryValidator.isValidColorFormat("#FF5722"))
        assertTrue(CategoryValidator.isValidColorFormat("#000"))
        assertTrue(CategoryValidator.isValidColorFormat("#FFFFFF"))
        assertTrue(CategoryValidator.isValidColorFormat("#abc123"))
    }
    
    @Test
    fun `isValidColorFormat returns false for invalid colors`() {
        assertFalse(CategoryValidator.isValidColorFormat("FF5722")) // Missing #
        assertFalse(CategoryValidator.isValidColorFormat("#GG5722")) // Invalid hex characters
        assertFalse(CategoryValidator.isValidColorFormat("#FF57")) // Wrong length
        assertFalse(CategoryValidator.isValidColorFormat("#FF57222")) // Too long
        assertFalse(CategoryValidator.isValidColorFormat("red")) // Color name
        assertFalse(CategoryValidator.isValidColorFormat("")) // Empty
    }
    
    @Test
    fun `canDeleteCategory returns invalid for default categories`() {
        val defaultCategory = createValidCategory().copy(isDefault = true)
        val result = CategoryValidator.canDeleteCategory(defaultCategory, hasTransactions = false)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Cannot delete default categories"))
    }
    
    @Test
    fun `canDeleteCategory returns invalid for categories with transactions`() {
        val customCategory = createValidCategory().copy(isDefault = false)
        val result = CategoryValidator.canDeleteCategory(customCategory, hasTransactions = true)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("has transactions") })
    }
    
    @Test
    fun `canDeleteCategory returns valid for custom categories without transactions`() {
        val customCategory = createValidCategory().copy(isDefault = false)
        val result = CategoryValidator.canDeleteCategory(customCategory, hasTransactions = false)
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `canEditCategory returns invalid for default categories`() {
        val defaultCategory = createValidCategory().copy(isDefault = true)
        val result = CategoryValidator.canEditCategory(defaultCategory)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Cannot edit default categories"))
    }
    
    @Test
    fun `canEditCategory returns valid for custom categories`() {
        val customCategory = createValidCategory().copy(isDefault = false)
        val result = CategoryValidator.canEditCategory(customCategory)
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    private fun createValidCategory(): Category {
        return Category(
            id = 1,
            name = "Test Category",
            color = "#FF5722",
            icon = "test_icon",
            isDefault = false
        )
    }
}