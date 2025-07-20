package com.expensetracker.domain.model

import java.util.Date

data class Category(
    val id: Long = 0,
    val name: String,
    val color: String,
    val icon: String?,
    val isDefault: Boolean = false,
    val createdAt: Date = Date()
) {
    companion object {
        // Default category names
        const val FOOD_DINING = "Food & Dining"
        const val TRANSPORTATION = "Transportation"
        const val SHOPPING = "Shopping"
        const val ENTERTAINMENT = "Entertainment"
        const val BILLS_UTILITIES = "Bills & Utilities"
        const val HEALTH_MEDICAL = "Health & Medical"
        const val EDUCATION = "Education"
        const val PERSONAL_CARE = "Personal Care"
        const val OTHER = "Other"
        
        // Default category colors (Material Design colors)
        const val COLOR_FOOD = "#FF5722" // Deep Orange
        const val COLOR_TRANSPORTATION = "#2196F3" // Blue
        const val COLOR_SHOPPING = "#E91E63" // Pink
        const val COLOR_ENTERTAINMENT = "#9C27B0" // Purple
        const val COLOR_BILLS = "#FF9800" // Orange
        const val COLOR_HEALTH = "#4CAF50" // Green
        const val COLOR_EDUCATION = "#3F51B5" // Indigo
        const val COLOR_PERSONAL_CARE = "#00BCD4" // Cyan
        const val COLOR_OTHER = "#607D8B" // Blue Grey
        
        // Icon names (using Material Icons)
        const val ICON_FOOD = "restaurant"
        const val ICON_TRANSPORTATION = "directions_car"
        const val ICON_SHOPPING = "shopping_bag"
        const val ICON_ENTERTAINMENT = "movie"
        const val ICON_BILLS = "receipt"
        const val ICON_HEALTH = "local_hospital"
        const val ICON_EDUCATION = "school"
        const val ICON_PERSONAL_CARE = "face"
        const val ICON_OTHER = "category"
    }
    
    /**
     * Returns true if this category can be deleted (not a default category)
     */
    fun canBeDeleted(): Boolean = !isDefault
    
    /**
     * Returns true if this category can be edited
     */
    fun canBeEdited(): Boolean = !isDefault
}