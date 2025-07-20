package com.expensetracker.data.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.expensetracker.R
import com.expensetracker.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing notifications related to SMS monitoring and transaction import
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        // Notification channels
        private const val CHANNEL_SMS_MONITORING = "sms_monitoring"
        private const val CHANNEL_IMPORT_PROGRESS = "import_progress"
        private const val CHANNEL_IMPORT_RESULTS = "import_results"
        
        // Notification IDs
        private const val NOTIFICATION_ID_NEW_TRANSACTIONS = 1001
        private const val NOTIFICATION_ID_PERMISSION_REQUIRED = 1002
        private const val NOTIFICATION_ID_IMPORT_PROGRESS = 1003
        private const val NOTIFICATION_ID_IMPORT_COMPLETED = 1004
        private const val NOTIFICATION_ID_IMPORT_FAILED = 1005
        private const val NOTIFICATION_ID_IMPORT_ERROR = 1006
        private const val NOTIFICATION_ID_UNCATEGORIZED_TRANSACTIONS = 1007
        private const val NOTIFICATION_ID_PROCESSING_ERROR = 1008
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Shows notification for new transactions found
     */
    fun showNewTransactionsNotification(importedCount: Int, duplicatesCount: Int) {
        val title = "New Transactions Found"
        val message = buildString {
            append("$importedCount new transactions imported")
            if (duplicatesCount > 0) {
                append(", $duplicatesCount duplicates skipped")
            }
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SMS_MONITORING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_NEW_TRANSACTIONS, notification)
    }
    
    /**
     * Shows notification when SMS permission is required
     */
    fun showPermissionRequiredNotification() {
        val title = "SMS Permission Required"
        val message = "Grant SMS permission to automatically import transactions"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SMS_MONITORING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_PERMISSION_REQUIRED, notification)
    }
    
    /**
     * Shows progress notification during import
     */
    fun showImportProgressNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_IMPORT_PROGRESS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Importing Transactions")
            .setContentText("Scanning SMS messages...")
            .setProgress(0, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_IMPORT_PROGRESS, notification)
    }
    
    /**
     * Dismisses the import progress notification
     */
    fun dismissImportProgressNotification() {
        NotificationManagerCompat.from(context)
            .cancel(NOTIFICATION_ID_IMPORT_PROGRESS)
    }
    
    /**
     * Shows notification when import is completed
     */
    fun showImportCompletedNotification(
        totalProcessed: Int,
        imported: Int,
        duplicates: Int,
        errors: Int
    ) {
        val title = "Import Completed"
        val message = buildString {
            append("Processed $totalProcessed SMS messages")
            if (imported > 0) {
                append(", imported $imported transactions")
            }
            if (duplicates > 0) {
                append(", skipped $duplicates duplicates")
            }
            if (errors > 0) {
                append(", $errors errors")
            }
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_IMPORT_RESULTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_IMPORT_COMPLETED, notification)
    }
    
    /**
     * Shows notification when import fails
     */
    fun showImportFailedNotification(errorMessage: String) {
        val title = "Import Failed"
        val message = "Failed to import transactions: $errorMessage"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_IMPORT_RESULTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_IMPORT_FAILED, notification)
    }
    
    /**
     * Shows notification for import errors
     */
    fun showImportErrorNotification(errorCount: Int) {
        val title = "Import Errors"
        val message = if (errorCount == 1) {
            "1 error occurred during import"
        } else {
            "$errorCount errors occurred during import"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_IMPORT_RESULTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_IMPORT_ERROR, notification)
    }
    
    /**
     * Shows notification for uncategorized transactions
     */
    fun showUncategorizedTransactionsNotification(uncategorizedCount: Int) {
        val title = "Uncategorized Transactions"
        val message = if (uncategorizedCount == 1) {
            "You have 1 uncategorized transaction"
        } else {
            "You have $uncategorizedCount uncategorized transactions"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add extra to navigate to categorization screen
            putExtra("navigate_to", "categorization")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SMS_MONITORING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification,
                "Categorize Now",
                pendingIntent
            )
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_UNCATEGORIZED_TRANSACTIONS, notification)
    }
    
    /**
     * Shows notification for processing errors
     */
    fun showProcessingErrorNotification(errorMessage: String) {
        val title = "Processing Error"
        val message = "Error processing transactions: $errorMessage"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_IMPORT_RESULTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_PROCESSING_ERROR, notification)
    }
    
    /**
     * Dismisses uncategorized transactions notification
     */
    fun dismissUncategorizedTransactionsNotification() {
        NotificationManagerCompat.from(context)
            .cancel(NOTIFICATION_ID_UNCATEGORIZED_TRANSACTIONS)
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // SMS Monitoring channel
            val smsMonitoringChannel = NotificationChannel(
                CHANNEL_SMS_MONITORING,
                "SMS Monitoring",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for SMS monitoring and new transactions"
            }
            
            // Import Progress channel
            val importProgressChannel = NotificationChannel(
                CHANNEL_IMPORT_PROGRESS,
                "Import Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progress notifications during transaction import"
            }
            
            // Import Results channel
            val importResultsChannel = NotificationChannel(
                CHANNEL_IMPORT_RESULTS,
                "Import Results",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Results and errors from transaction import"
            }
            
            notificationManager.createNotificationChannels(listOf(
                smsMonitoringChannel,
                importProgressChannel,
                importResultsChannel
            ))
        }
    }
}