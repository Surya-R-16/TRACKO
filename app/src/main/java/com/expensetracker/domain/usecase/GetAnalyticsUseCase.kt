package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.data.repository.model.MonthlySpendingSummary
import com.expensetracker.data.repository.model.SpendingTrendData
import com.expensetracker.data.repository.model.TransactionStats
import com.expensetracker.domain.usecase.base.UseCase
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Use case for retrieving various analytics data
 */
class GetAnalyticsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) : UseCase<GetAnalyticsUseCase.Params, AnalyticsResult>() {
    
    override suspend fun execute(parameters: Params): AnalyticsResult {
        return try {
            when (parameters.type) {
                AnalyticsType.MONTHLY_SUMMARY -> {
                    val summary = transactionRepository.getMonthlySpendingSummary(
                        parameters.year ?: getCurrentYear(),
                        parameters.month ?: getCurrentMonth()
                    )
                    AnalyticsResult.MonthlySummary(summary)
                }
                
                AnalyticsType.CATEGORY_SPENDING -> {
                    val startDate = parameters.startDate ?: getDefaultStartDate()
                    val endDate = parameters.endDate ?: Date()
                    val categorySpending = transactionRepository.getCategorySpendingSummary(startDate, endDate)
                    AnalyticsResult.CategorySpending(categorySpending)
                }
                
                AnalyticsType.SPENDING_TRENDS -> {
                    val startDate = parameters.startDate ?: getDefaultStartDate()
                    val endDate = parameters.endDate ?: Date()
                    val intervalDays = parameters.intervalDays ?: 7 // Default to weekly
                    val trends = transactionRepository.getSpendingTrends(startDate, endDate, intervalDays)
                    AnalyticsResult.SpendingTrends(trends)
                }
                
                AnalyticsType.TRANSACTION_STATS -> {
                    val stats = transactionRepository.getTransactionStats()
                    AnalyticsResult.TransactionStats(stats)
                }
                
                AnalyticsType.TOTAL_AMOUNT_BY_DATE_RANGE -> {
                    val startDate = parameters.startDate ?: getDefaultStartDate()
                    val endDate = parameters.endDate ?: Date()
                    val totalAmount = transactionRepository.getTotalAmountByDateRange(startDate, endDate)
                    AnalyticsResult.TotalAmount(totalAmount)
                }
                
                AnalyticsType.CATEGORY_AMOUNT_BY_DATE_RANGE -> {
                    val startDate = parameters.startDate ?: getDefaultStartDate()
                    val endDate = parameters.endDate ?: Date()
                    val category = parameters.category ?: throw IllegalArgumentException("Category is required")
                    val totalAmount = transactionRepository.getTotalAmountByCategoryAndDateRange(
                        category, startDate, endDate
                    )
                    AnalyticsResult.CategoryAmount(category, totalAmount)
                }
            }
        } catch (e: Exception) {
            AnalyticsResult.Error(e.message ?: "Failed to retrieve analytics data")
        }
    }
    
    private fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    private fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    
    private fun getDefaultStartDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1) // Default to last month
        return calendar.time
    }
    
    data class Params(
        val type: AnalyticsType,
        val year: Int? = null,
        val month: Int? = null,
        val startDate: Date? = null,
        val endDate: Date? = null,
        val category: String? = null,
        val intervalDays: Int? = null
    ) {
        companion object {
            fun monthlySummary(year: Int, month: Int) = Params(
                type = AnalyticsType.MONTHLY_SUMMARY,
                year = year,
                month = month
            )
            
            fun currentMonthlySummary() = Params(type = AnalyticsType.MONTHLY_SUMMARY)
            
            fun categorySpending(startDate: Date, endDate: Date) = Params(
                type = AnalyticsType.CATEGORY_SPENDING,
                startDate = startDate,
                endDate = endDate
            )
            
            fun spendingTrends(startDate: Date, endDate: Date, intervalDays: Int = 7) = Params(
                type = AnalyticsType.SPENDING_TRENDS,
                startDate = startDate,
                endDate = endDate,
                intervalDays = intervalDays
            )
            
            fun transactionStats() = Params(type = AnalyticsType.TRANSACTION_STATS)
            
            fun totalAmountByDateRange(startDate: Date, endDate: Date) = Params(
                type = AnalyticsType.TOTAL_AMOUNT_BY_DATE_RANGE,
                startDate = startDate,
                endDate = endDate
            )
            
            fun categoryAmountByDateRange(category: String, startDate: Date, endDate: Date) = Params(
                type = AnalyticsType.CATEGORY_AMOUNT_BY_DATE_RANGE,
                category = category,
                startDate = startDate,
                endDate = endDate
            )
        }
    }
    
    enum class AnalyticsType {
        MONTHLY_SUMMARY,
        CATEGORY_SPENDING,
        SPENDING_TRENDS,
        TRANSACTION_STATS,
        TOTAL_AMOUNT_BY_DATE_RANGE,
        CATEGORY_AMOUNT_BY_DATE_RANGE
    }
}

/**
 * Result of analytics operation
 */
sealed class AnalyticsResult {
    data class MonthlySummary(val summary: MonthlySpendingSummary) : AnalyticsResult()
    data class CategorySpending(val spending: Map<String, Double>) : AnalyticsResult()
    data class SpendingTrends(val trends: List<SpendingTrendData>) : AnalyticsResult()
    data class TransactionStats(val stats: com.expensetracker.data.repository.model.TransactionStats) : AnalyticsResult()
    data class TotalAmount(val amount: Double) : AnalyticsResult()
    data class CategoryAmount(val category: String, val amount: Double) : AnalyticsResult()
    data class Error(val message: String) : AnalyticsResult()
    
    val isSuccess: Boolean get() = this !is Error
    val isError: Boolean get() = this is Error
}