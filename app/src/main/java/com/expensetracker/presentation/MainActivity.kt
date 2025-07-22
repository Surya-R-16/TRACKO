package com.expensetracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.expensetracker.domain.model.Transaction
import com.expensetracker.presentation.categorization.CategorySelectionScreen
import com.expensetracker.presentation.dashboard.DashboardScreen
import com.expensetracker.presentation.theme.ExpenseTrackerTheme
import com.expensetracker.presentation.transaction.TransactionListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseTrackerApp()
                }
            }
        }
    }
}

@Composable
fun ExpenseTrackerApp() {
    // Simple navigation state
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    
    when (currentScreen) {
        Screen.Dashboard -> {
            DashboardScreen(
                onViewTransactionsClick = {
                    currentScreen = Screen.TransactionList
                },
                onImportSmsClick = { /* Handled by DashboardViewModel */ }
            )
        }
        
        Screen.TransactionList -> {
            TransactionListScreen(
                onTransactionClick = { transaction ->
                    selectedTransaction = transaction
                    currentScreen = Screen.Categorization
                },
                onBackPressed = {
                    currentScreen = Screen.Dashboard
                }
            )
        }
        
        Screen.Categorization -> {
            selectedTransaction?.let { transaction ->
                CategorySelectionScreen(
                    transaction = transaction,
                    onCategorySelected = { category ->
                        // Category selected, go back to transaction list
                        currentScreen = Screen.TransactionList
                    },
                    onBackPressed = {
                        currentScreen = Screen.TransactionList
                    }
                )
            }
        }
    }
}

enum class Screen {
    Dashboard,
    TransactionList,
    Categorization
}