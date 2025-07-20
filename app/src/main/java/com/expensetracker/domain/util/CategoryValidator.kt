package com.expensetracker.domain.util

import com.expensetracker.domain.model.Category

/**
 * Utility class for validating category data
 */
object CategoryValidator {
    
    private const val MAX_CATEGORY_NAME_LENGTH = 50
    private const val MIN_CATEGORY_NAME_LENGTH = 1
    
    /**
     * Validates a category and returns validation result
     */
    fun validateCategory(category: Category): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate name
        if (category.name.isBlank()) {
            errors.add("Category name cannot be empty")
        } else if (category.name.length < MIN_CATEGORY_NAME_LENGTH) {
            errors.add("Category name is too short")
        } else if (category.name.length > MAX_CATEGORY_NAME_LENGTH) {
            errors.add("Category name is too long (max $MAX_CATEGORY_NAME_LENGTH characters)")
        }
        
        // Validate color format
        if (!isValidColorFormat(category.color)) {
            errors.add("Invalid color format. Use hex format like #FF5722")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validates category name for uniqueness and format
     */
    fun validateCategoryName(name: String, existingNames: List<String> = emptyList()): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Category name cannot be empty")
        } else if (name.length < MIN_CATEGORY_NAME_LENGTH) {
            errors.add("Category name is too short")
        } else if (name.length > MAX_CATEGORY_NAME_LENGTH) {
            errors.add("Category name is too long (max $MAX_CATEGORY_NAME_LENGTH characters)")
        } else if (existingNames.contains(name)) {
            errors.add("Category name already exists")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Checks if a color string is in valid hex format
     */
    fun isValidColorFormat(color: String): Boolean {
        val hexColorRegex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$".toRegex()
        return hexColorRegex.matches(color)
    }
    
    /**
     * Checks if a category can be deleted
     */
    fun canDeleteCategory(category: Category, hasTransactions: Boolean): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (category.isDefault) {
            errors.add("Cannot delete default categories")
        }
        
        if (hasTransactions) {
            errors.add("Cannot delete category that has transactions. Please recategorize transactions first.")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Checks if a category can be edited
     */
    fun canEditCategory(category: Category): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (category.isDefault) {
            errors.add("Cannot edit default categories")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}