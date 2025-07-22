package com.expensetracker.presentation.categorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.domain.usecase.CategorizeTransactionUseCase
import com.expensetracker.domain.usecase.CategorizeTransactionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategorySelectionViewModel @Inject constructor(
    private val categorizeTransactionUseCase: CategorizeTransactionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategorySelectionUiState())
    val uiState: StateFlow<CategorySelectionUiState> = _uiState.asStateFlow()
    
    fun categorizeTransaction(transactionId: Long, category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Use real categorization use case
                val result = categorizeTransactionUseCase(
                    CategorizeTransactionUseCase.Params.single(
                        transactionId = transactionId,
                        category = category
                    )
                )
                
                if (result.isError) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = (result as CategorizeTransactionResult.Error).message
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to categorize transaction"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun reset() {
        _uiState.value = CategorySelectionUiState()
    }
}

data class CategorySelectionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)