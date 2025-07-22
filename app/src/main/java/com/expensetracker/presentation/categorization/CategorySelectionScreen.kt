package com.expensetracker.presentation.categorization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    transaction: Transaction,
    onCategorySelected: (String) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: CategorySelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Categories list
    val categories = listOf(
        "Food & Dining",
        "Transportation", 
        "Shopping",
        "Entertainment",
        "Bills & Utilities",
        "Health & Medical",
        "Education",
        "Personal Care",
        "Other"
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Simple top bar
        TopAppBar(
            title = { Text("Select Category") },
            navigationIcon = {
                TextButton(onClick = onBackPressed) {
                    Text("← Back")
                }
            }
        )
        
        // Transaction details card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "₹${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = transaction.merchantName ?: transaction.recipient ?: "Unknown Merchant",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transaction.dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = transaction.paymentMethod,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Success state
        else if (uiState.isSuccess) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓ Transaction Categorized!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackPressed) {
                        Text("Back to Transactions")
                    }
                }
            }
        }
        
        // Error state
        else if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.clearError() }) {
                    Text("Try Again")
                }
            }
        }
        
        // Category selection grid
        else {
            Text(
                text = "Choose a category:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = {
                            viewModel.categorizeTransaction(transaction.id, category)
                            onCategorySelected(category)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: String,
    onClick: () -> Unit
) {
    val categoryColors = mapOf(
        "Food & Dining" to Color(0xFFFF5722),
        "Transportation" to Color(0xFF2196F3),
        "Shopping" to Color(0xFFE91E63),
        "Entertainment" to Color(0xFF9C27B0),
        "Bills & Utilities" to Color(0xFFFF9800),
        "Health & Medical" to Color(0xFF4CAF50),
        "Education" to Color(0xFF3F51B5),
        "Personal Care" to Color(0xFF00BCD4),
        "Other" to Color(0xFF607D8B)
    )
    
    val color = categoryColors[category] ?: Color.Gray
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}