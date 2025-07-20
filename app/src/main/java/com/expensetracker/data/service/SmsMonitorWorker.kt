package com.expensetracker.data.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.expensetracker.data.permission.PermissionManager
import com.expensetracker.data.service.notification.NotificationHelper
import com.expensetracker.domain.usecase.ImportSmsTransactionsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for periodic SMS monitoring
 */
@HiltWorker
class SmsMonitorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val importSmsTransactionsUseCase: ImportSmsTransactionsUseCase,
    private val permissionManager: PermissionManager,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "sms_monitor_work"
        private const val INPUT_ENABLE_NOTIFICATIONS = "enable_notifications"
        private const val INPUT_IMPORT_INTERVAL_HOURS = "import_interval_hours"
        
        /**
         * Schedules periodic SMS monitoring
         */
        fun schedulePeriodicMonitoring(
            context: Context,
            intervalHours: Long = 6,
            enableNotifications: Boolean = true
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val inputData = Data.Builder()
                .putBoolean(INPUT_ENABLE_NOTIFICATIONS, enableNotifications)
                .putLong(INPUT_IMPORT_INTERVAL_HOURS, intervalHours)
                .build()
            
            val periodicWorkRequest = PeriodicWorkRequestBuilder<SmsMonitorWorker>(
                intervalHours, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
                )
        }
        
        /**
         * Cancels periodic SMS monitoring
         */
        fun cancelPeriodicMonitoring(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
        
        /**
         * Triggers one-time SMS import
         */
        fun triggerOneTimeImport(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            val inputData = Data.Builder()
                .putBoolean(INPUT_ENABLE_NOTIFICATIONS, true)
                .build()
            
            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SmsMonitorWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            
            WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            val enableNotifications = inputData.getBoolean(INPUT_ENABLE_NOTIFICATIONS, true)
            
            // Check if we have SMS permission
            if (!permissionManager.hasReadSmsPermission()) {
                if (enableNotifications) {
                    notificationHelper.showPermissionRequiredNotification()
                }
                return Result.failure()
            }
            
            // Import transactions with duplicate checking
            val params = ImportSmsTransactionsUseCase.Params.withDuplicateCheck()
            val result = importSmsTransactionsUseCase(params)
            
            when (result) {
                is ImportSmsTransactionsResult.Success -> {
                    val importResult = result.importResult
                    
                    if (enableNotifications) {
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
                    
                    // Return success even if there were some errors during import
                    Result.success()
                }
                
                is ImportSmsTransactionsResult.Error -> {
                    if (enableNotifications) {
                        notificationHelper.showImportErrorNotification(1)
                    }
                    Result.retry()
                }
            }
            
        } catch (e: Exception) {
            // Handle unexpected errors
            if (inputData.getBoolean(INPUT_ENABLE_NOTIFICATIONS, true)) {
                notificationHelper.showImportErrorNotification(1)
            }
            Result.retry()
        }
    }
}