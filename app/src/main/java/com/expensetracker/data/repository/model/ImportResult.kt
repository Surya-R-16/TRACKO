package com.expensetracker.data.repository.model

import com.expensetracker.domain.model.Transaction

/**
 * Data class representing the result of importing transactions from SMS
 */
data class ImportResult(
    val totalSmsProcessed: Int,
    val transactionsParsed: Int,
    val transactionsImported: Int,
    val duplicatesFound: Int,
    val errors: List<ImportError>,
    val importedTransactions: List<Transaction>,
    val duplicateTransactions: List<Transaction>
) {
    val successRate: Float get() = if (totalSmsProcessed > 0) (transactionsImported.toFloat() / totalSmsProcessed.toFloat()) * 100f else 0f
    val duplicateRate: Float get() = if (transactionsParsed > 0) (duplicatesFound.toFloat() / transactionsParsed.toFloat()) * 100f else 0f
    val hasErrors: Boolean get() = errors.isNotEmpty()
    val isSuccessful: Boolean get() = transactionsImported > 0 && !hasErrors
}

/**
 * Data class representing an import error
 */
data class ImportError(
    val type: ImportErrorType,
    val message: String,
    val smsContent: String? = null,
    val exception: Throwable? = null
)

/**
 * Enum representing different types of import errors
 */
enum class ImportErrorType {
    PERMISSION_DENIED,
    SMS_PARSING_FAILED,
    DATABASE_ERROR,
    VALIDATION_ERROR,
    DUPLICATE_DETECTION_ERROR,
    UNKNOWN_ERROR
}