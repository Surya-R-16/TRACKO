package com.expensetracker.data.service

import com.expensetracker.data.permission.PermissionManager
import com.expensetracker.data.repository.TransactionRepository
import com.expensetracker.data.service.notification.NotificationHelper
import com.expensetracker.data.sms.SmsContentProvider
import com.expensetracker.data.sms.SmsParserService
import com.expensetracker.data.sms.model.SmsMessage
import com.expensetracker.data.sms.util.SmsFilter
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.usecase.GetTransactionCountsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for automatically processing new SMS messages and transactions
 */
@Singleton
class AutomaticTransactionProcessor @Inject constructor(
    private val smsContentProvider: SmsContentProvider,
    private val smsParserService: SmsParserService,
    private val transactionRepository: TransactionRepository,
    private val permissionManager: PermissionManager,
    private val notificationHelper: NotificationHelper,
    private val getTransactionCountsUseCase: GetTransactionCountsUseCase
) {
    
    /**
     * Processes new SMS messages since the last scan
     */
    suspend fun processNewSmsMessages(lastScanTime: Long): ProcessingResult = withContext(Dispatchers.IO) {
        try {
            // Check permissions
            if (!permissionManager.hasReadSmsPermission()) {
                return@withContext ProcessingResult.PermissionDenied
            }
            
            // Get SMS messages since last scan
            val currentTime = System.currentTimeMillis()
            val newSmsMessages = smsContentProvider.getSmsMessagesByDateRange(lastScanTime, currentTime)
            
            if (newSmsMessages.isEmpty()) {
                return@withContext ProcessingResult.NoNewMessages
            }
            
            // Filter for transaction SMS
            val transactionSms = SmsFilter.filterTransactionSms(newSmsMessages)
            
            if (transactionSms.isEmpty()) {
                return@withContext ProcessingResult.NoTransactionMessages(newSmsMessages.size)
            }
            
            // Parse transactions
            val parsedTransactions = smsParserService.parseTransactionSmsMessages(transactionSms)
            
            if (parsedTransactions.isEmpty()) {
                return@withContext ProcessingResult.NoValidTransactions(transactionSms.size)
            }
            
            // Convert to domain transactions
            val domainTransactions = parsedTransactions.map { parsed ->
                Transaction(
                    amount = parsed.amount,
                    recipient = parsed.recipient,
                    merchantName = parsed.merchantName,
                    dateTime = parsed.dateTime,
                    transactionId = parsed.transactionId,
                    paymentMethod = parsed.paymentMethod,
                    category = null,
                    notes = null,
                    isCategorized = false,
                    smsContent = parsed.smsContent
                )
            }
            
            // Check for duplicates
            val uniqueTransactions = mutableListOf<Transaction>()
            val duplicateTransactions = mutableListOf<Transaction>()
            
            for (transaction in domainTransactions) {
                val isDuplicate = transactionRepository.isDuplicateTransaction(transaction)
                if (isDuplicate) {
                    duplicateTransactions.add(transaction)
                } else {
                    uniqueTransactions.add(transaction)
                }
            }
            
            // Insert unique transactions
            val insertedIds = if (uniqueTransactions.isNotEmpty()) {
                transactionRepository.insertTransactions(uniqueTransactions)
            } else {
                emptyList()
            }
            
            ProcessingResult.Success(
                totalSmsProcessed = newSmsMessages.size,
                transactionSmsFound = transactionSms.size,
                transactionsParsed = parsedTransactions.size,
                uniqueTransactions = uniqueTransactions.size,
                duplicateTransactions = duplicateTransactions.size,
                transactionsInserted = insertedIds.size,
                newTransactions = uniqueTransactions.take(insertedIds.size)
            )
            
        } catch (e: Exception) {
            ProcessingResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    /**
     * Processes a single SMS message immediately
     */
    suspend fun processSingleSmsMessage(smsMessage: SmsMessage): SingleMessageResult = withContext(Dispatchers.IO) {
        try {
            // Check if it's a transaction SMS
            if (!SmsFilter.isTransactionSms(smsMessage)) {
                return@withContext SingleMessageResult.NotTransactionSms
            }
            
            // Parse the transaction
            val parsedTransaction = smsParserService.parseTransactionSms(smsMessage)
                ?: return@withContext SingleMessageResult.ParsingFailed
            
            // Convert to domain transaction
            val domainTransaction = Transaction(
                amount = parsedTransaction.amount,
                recipient = parsedTransaction.recipient,
                merchantName = parsedTransaction.merchantName,
                dateTime = parsedTransaction.dateTime,
                transactionId = parsedTransaction.transactionId,
                paymentMethod = parsedTransaction.paymentMethod,
                category = null,
                notes = null,
                isCategorized = false,
                smsContent = parsedTransaction.smsContent
            )
            
            // Check for duplicates
            val isDuplicate = transactionRepository.isDuplicateTransaction(domainTransaction)
            if (isDuplicate) {
                return@withContext SingleMessageResult.Duplicate
            }
            
            // Insert the transaction
            val insertedId = transactionRepository.insertTransaction(domainTransaction)
            
            SingleMessageResult.Success(insertedId, domainTransaction)
            
        } catch (e: Exception) {
            SingleMessageResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    /**
     * Sends notifications for uncategorized transactions
     */
    suspend fun notifyUncategorizedTransactions() = withContext(Dispatchers.IO) {
        try {
            val transactionCounts = getTransactionCountsUseCase.getTransactionCounts()
            
            if (transactionCounts.hasUncategorizedTransactions) {
                notificationHelper.showUncategorizedTransactionsNotification(
                    transactionCounts.uncategorized
                )
            }
        } catch (e: Exception) {
            // Log error but don't throw - notification failure shouldn't break processing
        }
    }
    
    /**
     * Processes new transactions and sends appropriate notifications
     */
    suspend fun processAndNotify(lastScanTime: Long): ProcessingResult {
        val result = processNewSmsMessages(lastScanTime)
        
        when (result) {
            is ProcessingResult.Success -> {
                if (result.transactionsInserted > 0) {
                    // Send notification for new transactions
                    notificationHelper.showNewTransactionsNotification(
                        result.transactionsInserted,
                        result.duplicateTransactions
                    )
                    
                    // Send notification for uncategorized transactions after a delay
                    notifyUncategorizedTransactions()
                }
            }
            
            is ProcessingResult.Error -> {
                notificationHelper.showProcessingErrorNotification(result.message)
            }
            
            ProcessingResult.PermissionDenied -> {
                notificationHelper.showPermissionRequiredNotification()
            }
            
            else -> {
                // No action needed for other result types
            }
        }
        
        return result
    }
    
    /**
     * Gets processing statistics
     */
    suspend fun getProcessingStatistics(): ProcessingStatistics = withContext(Dispatchers.IO) {
        try {
            val transactionCounts = getTransactionCountsUseCase.getTransactionCounts()
            val totalTransactions = transactionRepository.getTotalTransactionCount()
            
            ProcessingStatistics(
                totalTransactions = totalTransactions,
                categorizedTransactions = transactionCounts.categorized,
                uncategorizedTransactions = transactionCounts.uncategorized,
                categorizationRate = transactionCounts.categorizationRate
            )
        } catch (e: Exception) {
            ProcessingStatistics(0, 0, 0, 0f)
        }
    }
}

/**
 * Result of processing new SMS messages
 */
sealed class ProcessingResult {
    data class Success(
        val totalSmsProcessed: Int,
        val transactionSmsFound: Int,
        val transactionsParsed: Int,
        val uniqueTransactions: Int,
        val duplicateTransactions: Int,
        val transactionsInserted: Int,
        val newTransactions: List<Transaction>
    ) : ProcessingResult()
    
    data class Error(val message: String) : ProcessingResult()
    object PermissionDenied : ProcessingResult()
    object NoNewMessages : ProcessingResult()
    data class NoTransactionMessages(val totalSmsProcessed: Int) : ProcessingResult()
    data class NoValidTransactions(val transactionSmsProcessed: Int) : ProcessingResult()
    
    val isSuccess: Boolean get() = this is Success
    val hasNewTransactions: Boolean get() = this is Success && transactionsInserted > 0
}

/**
 * Result of processing a single SMS message
 */
sealed class SingleMessageResult {
    data class Success(val transactionId: Long, val transaction: Transaction) : SingleMessageResult()
    data class Error(val message: String) : SingleMessageResult()
    object NotTransactionSms : SingleMessageResult()
    object ParsingFailed : SingleMessageResult()
    object Duplicate : SingleMessageResult()
    
    val isSuccess: Boolean get() = this is Success
}

/**
 * Processing statistics
 */
data class ProcessingStatistics(
    val totalTransactions: Int,
    val categorizedTransactions: Int,
    val uncategorizedTransactions: Int,
    val categorizationRate: Float
) {
    val hasTransactions: Boolean get() = totalTransactions > 0
    val hasUncategorizedTransactions: Boolean get() = uncategorizedTransactions > 0
}