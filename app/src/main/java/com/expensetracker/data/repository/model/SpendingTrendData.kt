package com.expensetracker.data.repository.model

import java.util.Date

/**
 * Data class representing spending trend data over time
 */
data class SpendingTrendData(
    val date: Date,
    val totalAmount: Double,
    val transactionCount: Int,
    val averageAmount: Double
)

/**
 * Data class representing transaction statistics
 */
data class TransactionStats(
    val totalTransactions: Int,
    val totalAmount: Double,
    val categorizedTransactions: Int,
    val uncategorizedTransactions: Int,
    val averageTransactionAmount: Double,
    val oldestTransactionDate: Date?,
    val newestTransactionDate: Date?,
    val paymentMethodBreakdown: Map<String, Int>,
    val categoryBreakdown: Map<String, Int>,
    val monthlyAverageSpending: Double
) {
    val categorizationRate: Float get() = if (totalTransactions > 0) (categorizedTransactions.toFloat() / totalTransactions.toFloat()) * 100f else 0f
    val hasTransactions: Boolean get() = totalTransactions > 0
}