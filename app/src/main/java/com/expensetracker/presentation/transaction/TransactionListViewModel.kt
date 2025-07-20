package com.expensetracker.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // For now, use test data to verify UI works
                // TODO: Replace with real data from use case
                val testTransactions = TestDataHelper.createSampleTransactions()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    transactions = testTransactions,
                    error = null
                )
                
                // Uncomment this when ready to use real data:
                /*
                getTransactionsUseCase(GetTransactionsUseCase.Params.all())
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load transactions"
                        )
                    }
                    .collect { transactions ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            transactions = transactions,
                            error = null
                        )
                    }
                */
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load transactions"
                )
            }
        }
    }
    
    fun refresh() {
        loadTransactions()
    }
}

data class TransactionListUiState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null
)