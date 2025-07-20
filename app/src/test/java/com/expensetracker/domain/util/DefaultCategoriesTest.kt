package com.expensetracker.domain.util

import com.expensetracker.domain.model.Category
import org.junit.Assert.*
import org.junit.Test

class DefaultCategoriesTest {
    
    @Test
    fun `getDefaultCategories returns correct number of categories`() {
        val categories = DefaultCategories.getDefaultCategories()
        assertEquals(9, categories.size)
    }
    
    @Test
    fun `getDefaultCategories returns all expected categories`() {
        val categories = DefaultCategories.getDefaultCategories()
        val categoryNames = categories.map { it.name }
        
        assertTrue(categoryNames.contains(Category.FOOD_DINING))
        assertTrue(categoryNames.contains(Category.TRANSPORTATION))
        assertTrue(categoryNames.contains(Category.SHOPPING))
        assertTrue(categoryNames.contains(Category.ENTERTAINMENT))
        assertTrue(categoryNames.contains(Category.BILLS_UTILITIES))
        assertTrue(categoryNames.contains(Category.HEALTH_MEDICAL))
        assertTrue(categoryNames.contains(Category.EDUCATION))
        assertTrue(categoryNames.contains(Category.PERSONAL_CARE))
        assertTrue(categoryNames.contains(Category.OTHER))
    }
    
    @Test
    fun `all default categories are marked as default`() {
        val categories = DefaultCategories.getDefaultCategories()
        assertTrue(categories.all { it.isDefault })
    }
    
    @Test
    fun `all default categories have valid colors`() {
        val categories = DefaultCategories.getDefaultCategories()
        categories.forEach { category ->
            assertTrue("Category ${category.name} has invalid color", 
                category.color.startsWith("#") && category.color.length == 7)
        }
    }
    
    @Test
    fun `all default categories have icons`() {
        val categories = DefaultCategories.getDefaultCategories()
        categories.forEach { category ->
            assertNotNull("Category ${category.name} has no icon", category.icon)
            assertFalse("Category ${category.name} has empty icon", category.icon!!.isBlank())
        }
    }
    
    @Test
    fun `getCategoryByName returns correct category`() {
        val foodCategory = DefaultCategories.getCategoryByName(Category.FOOD_DINING)
        assertNotNull(foodCategory)
        assertEquals(Category.FOOD_DINING, foodCategory!!.name)
        assertEquals(Category.COLOR_FOOD, foodCategory.color)
        assertEquals(Category.ICON_FOOD, foodCategory.icon)
    }
    
    @Test
    fun `getCategoryByName returns null for non-existent category`() {
        val category = DefaultCategories.getCategoryByName("Non-existent Category")
        assertNull(category)
    }
    
    @Test
    fun `getAllCategoryNames returns all category names`() {
        val names = DefaultCategories.getAllCategoryNames()
        assertEquals(9, names.size)
        assertTrue(names.contains(Category.FOOD_DINING))
        assertTrue(names.contains(Category.OTHER))
    }
    
    @Test
    fun `isDefaultCategory returns true for default categories`() {
        assertTrue(DefaultCategories.isDefaultCategory(Category.FOOD_DINING))
        assertTrue(DefaultCategories.isDefaultCategory(Category.TRANSPORTATION))
        assertTrue(DefaultCategories.isDefaultCategory(Category.OTHER))
    }
    
    @Test
    fun `isDefaultCategory returns false for custom categories`() {
        assertFalse(DefaultCategories.isDefaultCategory("Custom Category"))
        assertFalse(DefaultCategories.isDefaultCategory("My Category"))
    }
    
    @Test
    fun `getRandomColorForNewCategory returns valid hex color`() {
        val color = DefaultCategories.getRandomColorForNewCategory()
        assertTrue(color.startsWith("#"))
        assertEquals(7, color.length)
        
        // Test multiple times to ensure randomness works
        val colors = mutableSetOf<String>()
        repeat(10) {
            colors.add(DefaultCategories.getRandomColorForNewCategory())
        }
        // Should have some variety (not all the same color)
        assertTrue("Random color generator should produce variety", colors.size > 1)
    }
}