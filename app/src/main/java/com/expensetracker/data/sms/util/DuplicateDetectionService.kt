package com.expensetracker.data.sms.util

import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.domain.model.Transaction
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Service for detecting duplicate transactions
 */
@Singleton
class DuplicateDetectionService @Inject constructor() {
    
    companion object {
        // Default time window for duplicate detection (5 minutes in milliseconds)
        const val DEFAULT_TIME_WINDOW_MS = 5 * 60 * 1000L
        
        // Maximum time window for duplicate detection (30 minutes)
        const val MAX_TIME_WINDOW_MS = 30 * 60 * 1000L
        
        // Minimum time window for duplicate detection (1 minute)
        const val MIN_TIME_WINDOW_MS = 1 * 60 * 1000L
    }
    
    /**
     * Checks if a parsed transaction is a duplicate of any existing transaction
     */
    fun isDuplicate(
        parsedTransaction: ParsedTransaction,
        existingTransactions: List<Transaction>,
        timeWindowMs: Long = DEFAULT_TIME_WINDOW_MS
    ): Boolean {
        val validTimeWindow = timeWindowMs.coerceIn(MIN_TIME_WINDOW_MS, MAX_TIME_WINDOW_MS)
        
        return existingTransactions.any { existing ->
            isDuplicateTransaction(parsedTransaction, existing, validTimeWindow)
        }
    }
    
    /**
     * Finds potential duplicate transactions for a parsed transaction
     */
    fun findPotentialDuplicates(
        parsedTransaction: ParsedTransaction,
        existingTransactions: List<Transaction>,
        timeWindowMs: Long = DEFAULT_TIME_WINDOW_MS
    ): List<Transaction> {
        val validTimeWindow = timeWindowMs.coerceIn(MIN_TIME_WINDOW_MS, MAX_TIME_WINDOW_MS)
        
        return existingTransactions.filter { existing ->
            isDuplicateTransaction(parsedTransaction, existing, validTimeWindow)
        }
    }
    
    /**
     * Checks if two parsed transactions are duplicates of each other
     */
    fun areDuplicates(
        transaction1: ParsedTransaction,
        transaction2: ParsedTransaction,
        timeWindowMs: Long = DEFAULT_TIME_WINDOW_MS
    ): Boolean {
        val validTimeWindow = timeWindowMs.coerceIn(MIN_TIME_WINDOW_MS, MAX_TIME_WINDOW_MS)
        
        return isDuplicateParsedTransaction(transaction1, transaction2, validTimeWindow)
    }
    
    /**
     * Removes duplicate transactions from a list of parsed transactions
     */
    fun removeDuplicates(
        parsedTransactions: List<ParsedTransaction>,
        timeWindowMs: Long = DEFAULT_TIME_WINDOW_MS
    ): List<ParsedTransaction> {
        val validTimeWindow = timeWindowMs.coerceIn(MIN_TIME_WINDOW_MS, MAX_TIME_WINDOW_MS)
        val uniqueTransactions = mutableListOf<ParsedTransaction>()
        
        for (transaction in parsedTransactions.sortedBy { it.dateTime }) {
            val isDuplicate = uniqueTransactions.any { existing ->
                isDuplicateParsedTransaction(transaction, existing, validTimeWindow)
            }
            
            if (!isDuplicate) {
                uniqueTransactions.add(transaction)
            }
        }
        
        return uniqueTransactions
    }
    
    /**
     * Calculates duplicate confidence score between two transactions
     */
    fun calculateDuplicateConfidence(
        parsedTransaction: ParsedTransaction,
        existingTransaction: Transaction,
        timeWindowMs: Long = DEFAULT_TIME_WINDOW_MS
    ): Float {
        var confidence = 0.0f
        
        // Amount match (40% weight)
        if (abs(parsedTransaction.amount - existingTransaction.amount) < 0.01) {
            confidence += 0.4f
        }
        
        // Recipient/Merchant match (30% weight)
        val parsedIdentifier = parsedTransaction.getPrimaryIdentifier()
        val existingIdentifier = existingTransaction.getShortDescription()
        
        if (parsedIdentifier != null && existingIdentifier.isNotBlank()) {
            if (parsedIdentifier.equals(existingIdentifier, ignoreCase = true)) {
                confidence += 0.3f
            } else if (isSimilarIdentifier(parsedIdentifier, existingIdentifier)) {
                confidence += 0.15f
            }
        }
        
        // Time proximity (20% weight)
        val timeDifference = abs(parsedTransaction.dateTime.time - existingTransaction.dateTime.time)
        if (timeDifference <= timeWindowMs) {
            val timeScore = 1.0f - (timeDifference.toFloat() / timeWindowMs.toFloat())
            confidence += 0.2f * timeScore
        }
        
        // Transaction ID match (10% weight)
        if (!parsedTransaction.transactionId.isNullOrBlank() && 
            !existingTransaction.transactionId.isNullOrBlank() &&
            parsedTransaction.transactionId == existingTransaction.transactionId) {
            confidence += 0.1f
        }
        
        return confidence.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Gets duplicate detection summary for a list of parsed transactions
     */
    fun getDuplicateDetectionSummary(
        parsedTransactions: List<ParsedTransaction>,
        existingTransactions: List<Transaction>,
        timeWindowMs: Long = DEFAULT_TIME_WINDOW_MS
    ): DuplicateDetectionSummary {
        val validTimeWindow = timeWindowMs.coerceIn(MIN_TIME_WINDOW_MS, MAX_TIME_WINDOW_MS)
        
        val duplicates = mutableListOf<ParsedTransaction>()
        val unique = mutableListOf<ParsedTransaction>()
        val potentialDuplicates = mutableMapOf<ParsedTransaction, List<Transaction>>()
        
        for (parsed in parsedTransactions) {
            val potentials = findPotentialDuplicates(parsed, existingTransactions, validTimeWindow)
            
            if (potentials.isNotEmpty()) {
                duplicates.add(parsed)
                potentialDuplicates[parsed] = potentials
            } else {
                unique.add(parsed)
            }
        }
        
        return DuplicateDetectionSummary(
            totalParsed = parsedTransactions.size,
            uniqueTransactions = unique,
            duplicateTransactions = duplicates,
            potentialDuplicates = potentialDuplicates
        )
    }
    
    private fun isDuplicateTransaction(
        parsedTransaction: ParsedTransaction,
        existingTransaction: Transaction,
        timeWindowMs: Long
    ): Boolean {
        // Check amount match (must be exact)
        if (abs(parsedTransaction.amount - existingTransaction.amount) >= 0.01) {
            return false
        }
        
        // Check time window
        val timeDifference = abs(parsedTransaction.dateTime.time - existingTransaction.dateTime.time)
        if (timeDifference > timeWindowMs) {
            return false
        }
        
        // Check recipient/merchant match
        val parsedIdentifier = parsedTransaction.getPrimaryIdentifier()
        val existingIdentifier = existingTransaction.getShortDescription()
        
        if (parsedIdentifier != null && existingIdentifier.isNotBlank()) {
            return parsedIdentifier.equals(existingIdentifier, ignoreCase = true) ||
                   isSimilarIdentifier(parsedIdentifier, existingIdentifier)
        }
        
        // If we can't match identifiers but amount and time match, consider it a potential duplicate
        return true
    }
    
    private fun isDuplicateParsedTransaction(
        transaction1: ParsedTransaction,
        transaction2: ParsedTransaction,
        timeWindowMs: Long
    ): Boolean {
        // Check amount match
        if (abs(transaction1.amount - transaction2.amount) >= 0.01) {
            return false
        }
        
        // Check time window
        val timeDifference = abs(transaction1.dateTime.time - transaction2.dateTime.time)
        if (timeDifference > timeWindowMs) {
            return false
        }
        
        // Check recipient/merchant match
        val identifier1 = transaction1.getPrimaryIdentifier()
        val identifier2 = transaction2.getPrimaryIdentifier()
        
        if (identifier1 != null && identifier2 != null) {
            return identifier1.equals(identifier2, ignoreCase = true) ||
                   isSimilarIdentifier(identifier1, identifier2)
        }
        
        return true
    }
    
    private fun isSimilarIdentifier(identifier1: String, identifier2: String): Boolean {
        val normalized1 = identifier1.uppercase().replace(Regex("[^A-Z0-9]"), "")
        val normalized2 = identifier2.uppercase().replace(Regex("[^A-Z0-9]"), "")
        
        // Check if one contains the other
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
            return true
        }
        
        // Check Levenshtein distance for similar strings
        if (normalized1.length >= 3 && normalized2.length >= 3) {
            val distance = calculateLevenshteinDistance(normalized1, normalized2)
            val maxLength = maxOf(normalized1.length, normalized2.length)
            val similarity = 1.0 - (distance.toDouble() / maxLength.toDouble())
            return similarity >= 0.8 // 80% similarity threshold
        }
        
        return false
    }
    
    private fun calculateLevenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) {
            dp[i][0] = i
        }
        
        for (j in 0..str2.length) {
            dp[0][j] = j
        }
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                if (str1[i - 1] == str2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[str1.length][str2.length]
    }
}

/**
 * Data class representing duplicate detection results
 */
data class DuplicateDetectionSummary(
    val totalParsed: Int,
    val uniqueTransactions: List<ParsedTransaction>,
    val duplicateTransactions: List<ParsedTransaction>,
    val potentialDuplicates: Map<ParsedTransaction, List<Transaction>>
) {
    val uniqueCount: Int get() = uniqueTransactions.size
    val duplicateCount: Int get() = duplicateTransactions.size
    val duplicatePercentage: Float get() = if (totalParsed > 0) (duplicateCount.toFloat() / totalParsed.toFloat()) * 100f else 0f
}