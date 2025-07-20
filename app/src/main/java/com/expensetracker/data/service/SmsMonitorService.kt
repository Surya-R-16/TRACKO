package com.expensetracker.data.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.expensetracker.data.permission.PermissionManager
import com.expensetracker.data.service.notification.NotificationHelper
import com.expensetracker.domain.usecase.ImportSmsTransactionsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background service for monitoring SMS messages and importing transactions
 */
@AndroidEntryPoint
class SmsMonitorService : JobIntentService() {
    
    @Inject
    lateinit var importSmsTransactionsUseCase: ImportSmsTransactionsUseCase
    
    @Inject
    lateinit var permissionManager: PermissionManager
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val JOB_ID = 1000
        private const val ACTION_SCAN_SMS = "com.expensetracker.action.SCAN_SMS"
        private const val ACTION_IMPORT_TRANSACTIONS = "com.expensetracker.action.IMPORT_TRANSACTIONS"
        
        /**
         * Starts the SMS monitoring service
         */
        fun startSmsMonitoring(context: Context) {
            val intent = Intent(context, SmsMonitorService::class.java).apply {
                action = ACTION_SCAN_SMS
            }
            enqueueWork(context, SmsMonitorService::class.java, JOB_ID, intent)
        }
        
        /**
         * Starts transaction import
         */
        fun startTransactionImport(context: Context) {
            val intent = Intent(context, SmsMonitorService::class.java).apply {
                action = ACTION_IMPORT_TRANSACTIONS
            }
            enqueueWork(context, SmsMonitorService::class.java, JOB_ID, intent)
        }
    }
    
    override fun onHandleWork(intent: Intent) {
        when (intent.action) {
            ACTION_SCAN_SMS -> handleSmsScanning()
            ACTION_IMPORT_TRANSACTIONS -> handleTransactionImport()
            else -> {
                // Default action - scan SMS
                handleSmsScanning()
            }
        }
    }
    
    private fun handleSmsScanning() {
        serviceScope.launch {
            try {
                // Check if we have SMS permission
                if (!permissionManager.hasReadSmsPermission()) {
                    notificationHelper.showPermissionRequiredNotification()
                    return@launch
                }
                
                // Import transactions with duplicate checking
                val params = ImportSmsTransactionsUseCase.Params.withDuplicateCheck()
                val result = importSmsTransactionsUseCase(params)
                
                when (result) {
                    is ImportSmsTransactionsResult.Success -> {
                        val importResult = result.importResult
                        
                        if (importResult.transactionsImported > 0) {
                            // Show notification for new transactions
                            notificationHelper.showNewTransactionsNotification(
                                importResult.transactionsImported,
                                importResult.duplicatesFound
                            )
                        }
                        
                        if (importResult.hasErrors) {
                            // Show notification for import errors
                            notificationHelper.showImportErrorNotification(importResult.errors.size)
                        }
                    }
                    
                    is ImportSmsTransactionsResult.Error -> {
                        // Show error notification
                        notificationHelper.showImportErrorNotification(1)
                    }
                }
                
            } catch (e: Exception) {
                // Handle unexpected errors
                notificationHelper.showImportErrorNotification(1)
            }
        }
    }
    
    private fun handleTransactionImport() {
        serviceScope.launch {
            try {
                // Check permissions first
                if (!permissionManager.hasReadSmsPermission()) {
                    notificationHelper.showPermissionRequiredNotification()
                    return@launch
                }
                
                // Show progress notification
                notificationHelper.showImportProgressNotification()
                
                // Import transactions
                val params = ImportSmsTransactionsUseCase.Params.withDuplicateCheck()
                val result = importSmsTransactionsUseCase(params)
                
                // Dismiss progress notification
                notificationHelper.dismissImportProgressNotification()
                
                when (result) {
                    is ImportSmsTransactionsResult.Success -> {
                        val importResult = result.importResult
                        
                        // Show completion notification
                        notificationHelper.showImportCompletedNotification(
                            totalProcessed = importResult.totalSmsProcessed,
                            imported = importResult.transactionsImported,
                            duplicates = importResult.duplicatesFound,
                            errors = importResult.errors.size
                        )
                    }
                    
                    is ImportSmsTransactionsResult.Error -> {
                        notificationHelper.showImportFailedNotification(result.message)
                    }
                }
                
            } catch (e: Exception) {
                notificationHelper.dismissImportProgressNotification()
                notificationHelper.showImportFailedNotification(e.message ?: "Unknown error")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cancel any ongoing coroutines
        serviceScope.coroutineContext.cancel()
    }
}