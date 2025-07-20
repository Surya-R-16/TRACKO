package com.expensetracker.data.repository

import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.mapper.toDomain
import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.data.sms.util.DuplicateDetectionService
import com.expensetracker.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository-level duplicate transaction checker
 */
@Singleton
class DuplicateTransactionChecker @Inject constructor(
    private val transactionDao: TransactionDao,
    private val duplicateDetectionService: DuplicateDetectionService
) {
    
    /**
     * Checks if a parsed transaction is a duplicate by querying the database
     */
    suspend fun isDuplicateTransaction(parsedTransaction: ParsedTransaction): Boolean = withContext(Dispatchers.IO) {
        val potentialDuplicate = transactionDao.findPotentialDuplicate(
            amount = parsedTransaction.amount,
            recipient = parsedTransaction.recipient,
            merchantName = parsedTransaction.merchantName,
            dateTime = parsedTransaction.dateTime.time,
            timeWindowMs = DuplicateDetectionService.DEFAULT_TIME_WINDOW_MS
        )
        
        return@withContext potentialDuplicate != null
    }
    
    /**
     * Finds potential duplicate transactions in the database
     */
    suspend fun findPotentialDuplicates(parsedTransaction: ParsedTransaction): List<Transaction> = withContext(Dispatchers.IO) {
        val databaseDuplicate = transactionDao.findPotentialDuplicate(
            amount = parsedTransaction.amount,
            recipient = parsedTransaction.recipient,
            merchantName = parsedTransaction.merchantName,
            dateTime = parsedTransaction.dateTime.time,
            timeWindowMs = DuplicateDetectionService.DEFAULT_TIME_WINDOW_MS
        )
        
        return@withContext if (databaseDuplicate != null) {
            listOf(databaseDuplicate.toDomain())
        } else {
            emptyList()
        }
    }
    
    /**
     * Filters out duplicate transactions from a list of parsed transactions
     */
    suspend fun filterDuplicates(parsedTransactions: List<ParsedTransaction>): List<ParsedTransaction> = withContext(Dispatchers.IO) {
        val uniqueTransactions = mutableListOf<ParsedTransaction>()
        
        for (parsedTransaction in parsedTransactions) {
            if (!isDuplicateTransaction(parsedTransaction)) {
                uniqueTransactions.add(parsedTransaction)
            }
        }
        
        return@withContext uniqueTransactions
    }
    
    /**
     * Gets detailed duplicate analysis for a list of parsed transactions
     */
    suspend fun analyzeDuplicates(parsedTransactions: List<ParsedTransaction>): DuplicateAnalysisResult = withContext(Dispatchers.IO) {
        val uniqueTransactions = mutableListOf<ParsedTransaction>()
        val duplicateTransactions = mutableListOf<ParsedTransaction>()
        val duplicateDetails = mutableMapOf<ParsedTransaction, List<Transaction>>()
        
        for (parsedTransaction in parsedTransactions) {
            val potentialDuplicates = findPotentialDuplicates(parsedTransaction)
            
            if (potentialDuplicates.isNotEmpty()) {
                duplicateTransactions.add(parsedTransaction)
                duplicateDetails[parsedTransaction] = potentialDuplicates
            } else {
                uniqueTransactions.add(parsedTransaction)
            }
        }
        
        return@withContext DuplicateAnalysisResult(
            totalTransactions = parsedTransactions.size,
            uniqueTransactions = uniqueTransactions,
            duplicateTransactions = duplicateTransactions,
            duplicateDetails = duplicateDetails
        )
    }
    
    /**
     * Calculates duplicate confidence score for a parsed transaction
     */
    suspend fun calculateDuplicateConfidence(parsedTransaction: ParsedTransaction): Float = withContext(Dispatchers.IO) {
        val potentialDuplicates = findPotentialDuplicates(parsedTransaction)
        
        if (potentialDuplicates.isEmpty()) {
            return@withContext 0.0f
        }
        
        // Return the highest confidence score among all potential duplicates
        return@withContext potentialDuplicates.maxOfOrNull { existingTransaction ->
            duplicateDetectionService.calculateDuplicateConfidence(parsedTransaction, existingTransaction)
        } ?: 0.0f
    }
}

/**
 * Data class representing duplicate analysis results
 */
data class DuplicateAnalysisResult(
    val totalTransactions: Int,
    val uniqueTransactions: List<ParsedTransaction>,
    val duplicateTransactions: List<ParsedTransaction>,
    val duplicateDetails: Map<ParsedTransaction, List<Transaction>>
) {
    val uniqueCount: Int get() = uniqueTransactions.size
    val duplicateCount: Int get() = duplicateTransactions.size
    val duplicatePercentage: Float get() = if (totalTransactions > 0) (duplicateCount.toFloat() / totalTransactions.toFloat()) * 100f else 0f
    
    fun hasNoDuplicates(): Boolean = duplicateCount == 0
    fun hasDuplicates(): Boolean = duplicateCount > 0
}