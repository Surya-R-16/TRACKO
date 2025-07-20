package com.expensetracker.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class CategoryTest {
    
    @Test
    fun `canBeDeleted returns false for default categories`() {
        val defaultCategory = Category(
            name = "Food & Dining",
            color = "#FF5722",
            icon = "restaurant",
            isDefault = true
        )
        
        assertFalse(defaultCategory.canBeDeleted())
    }
    
    @Test
    fun `canBeDeleted returns true for custom categories`() {
        val customCategory = Category(
            name = "Custom Category",
            color = "#FF5722",
            icon = "custom",
            isDefault = false
        )
        
        assertTrue(customCategory.canBeDeleted())
    }
    
    @Test
    fun `canBeEdited returns false for default categories`() {
        val defaultCategory = Category(
            name = "Food & Dining",
            color = "#FF5722",
            icon = "restaurant",
            isDefault = true
        )
        
        assertFalse(defaultCategory.canBeEdited())
    }
    
    @Test
    fun `canBeEdited returns true for custom categories`() {
        val customCategory = Category(
            name = "Custom Category",
            color = "#FF5722",
            icon = "custom",
            isDefault = false
        )
        
        assertTrue(customCategory.canBeEdited())
    }
    
    @Test
    fun `category constants are properly defined`() {
        assertEquals("Food & Dining", Category.FOOD_DINING)
        assertEquals("Transportation", Category.TRANSPORTATION)
        assertEquals("Shopping", Category.SHOPPING)
        assertEquals("Entertainment", Category.ENTERTAINMENT)
        assertEquals("Bills & Utilities", Category.BILLS_UTILITIES)
        assertEquals("Health & Medical", Category.HEALTH_MEDICAL)
        assertEquals("Education", Category.EDUCATION)
        assertEquals("Personal Care", Category.PERSONAL_CARE)
        assertEquals("Other", Category.OTHER)
    }
    
    @Test
    fun `color constants are valid hex colors`() {
        assertTrue(Category.COLOR_FOOD.startsWith("#"))
        assertTrue(Category.COLOR_TRANSPORTATION.startsWith("#"))
        assertTrue(Category.COLOR_SHOPPING.startsWith("#"))
        assertTrue(Category.COLOR_ENTERTAINMENT.startsWith("#"))
        assertTrue(Category.COLOR_BILLS.startsWith("#"))
        assertTrue(Category.COLOR_HEALTH.startsWith("#"))
        assertTrue(Category.COLOR_EDUCATION.startsWith("#"))
        assertTrue(Category.COLOR_PERSONAL_CARE.startsWith("#"))
        assertTrue(Category.COLOR_OTHER.startsWith("#"))
        
        assertEquals(7, Category.COLOR_FOOD.length) // #RRGGBB format
    }
    
    @Test
    fun `icon constants are properly defined`() {
        assertNotNull(Category.ICON_FOOD)
        assertNotNull(Category.ICON_TRANSPORTATION)
        assertNotNull(Category.ICON_SHOPPING)
        assertNotNull(Category.ICON_ENTERTAINMENT)
        assertNotNull(Category.ICON_BILLS)
        assertNotNull(Category.ICON_HEALTH)
        assertNotNull(Category.ICON_EDUCATION)
        assertNotNull(Category.ICON_PERSONAL_CARE)
        assertNotNull(Category.ICON_OTHER)
    }
}