package com.expensetracker.domain.util

import com.expensetracker.domain.model.Category

/**
 * Utility object for managing default categories
 */
object DefaultCategories {
    
    /**
     * Returns the list of default categories that should be created when the app is first installed
     */
    fun getDefaultCategories(): List<Category> {
        return listOf(
            Category(
                name = Category.FOOD_DINING,
                color = Category.COLOR_FOOD,
                icon = Category.ICON_FOOD,
                isDefault = true
            ),
            Category(
                name = Category.TRANSPORTATION,
                color = Category.COLOR_TRANSPORTATION,
                icon = Category.ICON_TRANSPORTATION,
                isDefault = true
            ),
            Category(
                name = Category.SHOPPING,
                color = Category.COLOR_SHOPPING,
                icon = Category.ICON_SHOPPING,
                isDefault = true
            ),
            Category(
                name = Category.ENTERTAINMENT,
                color = Category.COLOR_ENTERTAINMENT,
                icon = Category.ICON_ENTERTAINMENT,
                isDefault = true
            ),
            Category(
                name = Category.BILLS_UTILITIES,
                color = Category.COLOR_BILLS,
                icon = Category.ICON_BILLS,
                isDefault = true
            ),
            Category(
                name = Category.HEALTH_MEDICAL,
                color = Category.COLOR_HEALTH,
                icon = Category.ICON_HEALTH,
                isDefault = true
            ),
            Category(
                name = Category.EDUCATION,
                color = Category.COLOR_EDUCATION,
                icon = Category.ICON_EDUCATION,
                isDefault = true
            ),
            Category(
                name = Category.PERSONAL_CARE,
                color = Category.COLOR_PERSONAL_CARE,
                icon = Category.ICON_PERSONAL_CARE,
                isDefault = true
            ),
            Category(
                name = Category.OTHER,
                color = Category.COLOR_OTHER,
                icon = Category.ICON_OTHER,
                isDefault = true
            )
        )
    }
    
    /**
     * Returns a category by name from the default categories
     */
    fun getCategoryByName(name: String): Category? {
        return getDefaultCategories().find { it.name == name }
    }
    
    /**
     * Returns all available category names
     */
    fun getAllCategoryNames(): List<String> {
        return getDefaultCategories().map { it.name }
    }
    
    /**
     * Checks if a category name is a default category
     */
    fun isDefaultCategory(name: String): Boolean {
        return getAllCategoryNames().contains(name)
    }
    
    /**
     * Returns a random color for a new custom category
     */
    fun getRandomColorForNewCategory(): String {
        val colors = listOf(
            "#F44336", // Red
            "#E91E63", // Pink
            "#9C27B0", // Purple
            "#673AB7", // Deep Purple
            "#3F51B5", // Indigo
            "#2196F3", // Blue
            "#03A9F4", // Light Blue
            "#00BCD4", // Cyan
            "#009688", // Teal
            "#4CAF50", // Green
            "#8BC34A", // Light Green
            "#CDDC39", // Lime
            "#FFEB3B", // Yellow
            "#FFC107", // Amber
            "#FF9800", // Orange
            "#FF5722", // Deep Orange
            "#795548", // Brown
            "#9E9E9E", // Grey
            "#607D8B"  // Blue Grey
        )
        return colors.random()
    }
}