package com.expensetracker.data.mapper

import com.expensetracker.data.local.entity.Transaction as TransactionEntity
import com.expensetracker.domain.model.Transaction as TransactionDomain

/**
 * Extension function to convert domain Transaction to data entity Transaction
 */
fun TransactionDomain.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        recipient = recipient,
        merchantName = merchantName,
        dateTime = dateTime,
        transactionId = transactionId,
        paymentMethod = paymentMethod,
        category = category,
        notes = notes,
        isCategorized = isCategorized,
        smsContent = smsContent,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extension function to convert data entity Transaction to domain Transaction
 */
fun TransactionEntity.toDomain(): TransactionDomain {
    return TransactionDomain(
        id = id,
        amount = amount,
        recipient = recipient,
        merchantName = merchantName,
        dateTime = dateTime,
        transactionId = transactionId,
        paymentMethod = paymentMethod,
        category = category,
        notes = notes,
        isCategorized = isCategorized,
        smsContent = smsContent,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extension function to convert list of entities to list of domain models
 */
fun List<TransactionEntity>.toDomain(): List<TransactionDomain> {
    return map { it.toDomain() }
}

/**
 * Extension function to convert list of domain models to list of entities
 */
fun List<TransactionDomain>.toEntity(): List<TransactionEntity> {
    return map { it.toEntity() }
}