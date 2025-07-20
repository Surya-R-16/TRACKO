package com.expensetracker.data.mapper

import com.expensetracker.data.local.entity.Category as CategoryEntity
import com.expensetracker.domain.model.Category as CategoryDomain

/**
 * Extension function to convert domain Category to data entity Category
 */
fun CategoryDomain.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        color = color,
        icon = icon,
        isDefault = isDefault,
        createdAt = createdAt
    )
}

/**
 * Extension function to convert data entity Category to domain Category
 */
fun CategoryEntity.toDomain(): CategoryDomain {
    return CategoryDomain(
        id = id,
        name = name,
        color = color,
        icon = icon,
        isDefault = isDefault,
        createdAt = createdAt
    )
}

/**
 * Extension function to convert list of entities to list of domain models
 */
fun List<CategoryEntity>.toDomain(): List<CategoryDomain> {
    return map { it.toDomain() }
}

/**
 * Extension function to convert list of domain models to list of entities
 */
fun List<CategoryDomain>.toEntity(): List<CategoryEntity> {
    return map { it.toEntity() }
}