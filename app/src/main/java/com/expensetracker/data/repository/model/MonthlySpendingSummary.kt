package com.expensetracker.data.repository.model

/**
 * Data class representing monthly spending summary
 */
data class MonthlySpendingSummary(
    val year: Int,
    val month: Int,
    val totalSpent: Double,
    val totalTransactions: Int,
    val categorizedTransactions: Int,
    val uncategorizedTransactions: Int,
    val categoryBreakdown: Map<String, CategorySpendingData>,
    val dailySpending: List<DailySpendingData>,
    val averageDailySpending: Double,
    val highestSpendingDay: DailySpendingData?,
    val mostUsedPaymentMethod: String?,
    val topMerchant: String?
) {
    val categorizationRate: Float get() = if (totalTransactions > 0) (categorizedTransactions.toFloat() / totalTransactions.toFloat()) * 100f else 0f
    val hasSpending: Boolean get() = totalSpent > 0
    val hasTransactions: Boolean get() = totalTransactions > 0
}

/**
 * Data class representing category spending data
 */
data class CategorySpendingData(
    val categoryName: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val averageAmount: Double,
    val percentage: Float
)

/**
 * Data class representing daily spending data
 */
data class DailySpendingData(
    val day: Int,
    val totalAmount: Double,
    val transactionCount: Int
)