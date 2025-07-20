package com.expensetracker.domain.usecase

import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

/**
 * Use case for retrieving transactions with various filtering options
 */
class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FlowUseCase<GetTransactionsUseCase.Params, List<Transaction>>() {
    
    override fun execute(parameters: Params): Flow<List<Transaction>> {
        return when (parameters.filterType) {
            FilterType.ALL -> transactionRepository.getAllTransactions()
            FilterType.UNCATEGORIZED -> transactionRepository.getUncategorizedTransactions()
            FilterType.CATEGORIZED -> transactionRepository.getCategorizedTransactions()
            FilterType.BY_CATEGORY -> transactionRepository.getTransactionsByCategory(parameters.category!!)
            FilterType.BY_DATE_RANGE -> transactionRepository.getTransactionsByDateRange(
                parameters.startDate!!, 
                parameters.endDate!!
            )
            FilterType.BY_CATEGORY_AND_DATE -> transactionRepository.getTransactionsByCategoryAndDateRange(
                parameters.category!!,
                parameters.startDate!!,
                parameters.endDate!!
            )
            FilterType.SEARCH -> transactionRepository.searchTransactions(parameters.searchQuery!!)
        }
    }
    
    data class Params(
        val filterType: FilterType,
        val category: String? = null,
        val startDate: Date? = null,
        val endDate: Date? = null,
        val searchQuery: String? = null
    ) {
        companion object {
            fun all() = Params(FilterType.ALL)
            fun uncategorized() = Params(FilterType.UNCATEGORIZED)
            fun categorized() = Params(FilterType.CATEGORIZED)
            fun byCategory(category: String) = Params(FilterType.BY_CATEGORY, category = category)
            fun byDateRange(startDate: Date, endDate: Date) = Params(
                FilterType.BY_DATE_RANGE, 
                startDate = startDate, 
                endDate = endDate
            )
            fun byCategoryAndDate(category: String, startDate: Date, endDate: Date) = Params(
                FilterType.BY_CATEGORY_AND_DATE,
                category = category,
                startDate = startDate,
                endDate = endDate
            )
            fun search(query: String) = Params(FilterType.SEARCH, searchQuery = query)
        }
    }
    
    enum class FilterType {
        ALL,
        UNCATEGORIZED,
        CATEGORIZED,
        BY_CATEGORY,
        BY_DATE_RANGE,
        BY_CATEGORY_AND_DATE,
        SEARCH
    }
}