package com.expensetracker.data.service

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.expensetracker.data.permission.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for SMS monitoring services and background tasks
 */
@Singleton
class SmsMonitorManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
    private val sharedPreferences: SharedPreferences
) {
    
    companion object {
        private const val PREF_SMS_MONITORING_ENABLED = "sms_monitoring_enabled"
        private const val PREF_MONITORING_INTERVAL_HOURS = "monitoring_interval_hours"
        private const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val PREF_LAST_SCAN_TIME = "last_scan_time"
        
        private const val DEFAULT_INTERVAL_HOURS = 6L
    }
    
    /**
     * Checks if SMS monitoring is currently enabled
     */
    fun isSmsMonitoringEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_SMS_MONITORING_ENABLED, false)
    }
    
    /**
     * Enables SMS monitoring with specified interval
     */
    fun enableSmsMonitoring(intervalHours: Long = DEFAULT_INTERVAL_HOURS) {
        if (!permissionManager.hasReadSmsPermission()) {
            throw IllegalStateException("SMS permission is required to enable monitoring")
        }
        
        // Save preferences
        sharedPreferences.edit()
            .putBoolean(PREF_SMS_MONITORING_ENABLED, true)
            .putLong(PREF_MONITORING_INTERVAL_HOURS, intervalHours)
            .apply()
        
        // Schedule periodic monitoring
        SmsMonitorWorker.schedulePeriodicMonitoring(
            context = context,
            intervalHours = intervalHours,
            enableNotifications = areNotificationsEnabled()
        )
    }
    
    /**
     * Disables SMS monitoring
     */
    fun disableSmsMonitoring() {
        // Save preferences
        sharedPreferences.edit()
            .putBoolean(PREF_SMS_MONITORING_ENABLED, false)
            .apply()
        
        // Cancel periodic monitoring
        SmsMonitorWorker.cancelPeriodicMonitoring(context)
    }
    
    /**
     * Triggers a one-time SMS scan
     */
    fun triggerManualScan() {
        if (!permissionManager.hasReadSmsPermission()) {
            throw IllegalStateException("SMS permission is required to scan SMS")
        }
        
        SmsMonitorWorker.triggerOneTimeImport(context)
        updateLastScanTime()
    }
    
    /**
     * Starts the SMS monitoring service for immediate processing
     */
    fun startImmediateSmsMonitoring() {
        if (!permissionManager.hasReadSmsPermission()) {
            throw IllegalStateException("SMS permission is required to start monitoring")
        }
        
        SmsMonitorService.startSmsMonitoring(context)
        updateLastScanTime()
    }
    
    /**
     * Starts transaction import service
     */
    fun startTransactionImport() {
        if (!permissionManager.hasReadSmsPermission()) {
            throw IllegalStateException("SMS permission is required to import transactions")
        }
        
        SmsMonitorService.startTransactionImport(context)
    }
    
    /**
     * Gets the monitoring interval in hours
     */
    fun getMonitoringIntervalHours(): Long {
        return sharedPreferences.getLong(PREF_MONITORING_INTERVAL_HOURS, DEFAULT_INTERVAL_HOURS)
    }
    
    /**
     * Sets the monitoring interval
     */
    fun setMonitoringInterval(intervalHours: Long) {
        sharedPreferences.edit()
            .putLong(PREF_MONITORING_INTERVAL_HOURS, intervalHours)
            .apply()
        
        // If monitoring is enabled, reschedule with new interval
        if (isSmsMonitoringEnabled()) {
            enableSmsMonitoring(intervalHours)
        }
    }
    
    /**
     * Checks if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_NOTIFICATIONS_ENABLED, true)
    }
    
    /**
     * Enables or disables notifications
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled)
            .apply()
        
        // If monitoring is enabled, reschedule with new notification setting
        if (isSmsMonitoringEnabled()) {
            enableSmsMonitoring(getMonitoringIntervalHours())
        }
    }
    
    /**
     * Gets the last scan time
     */
    fun getLastScanTime(): Long {
        return sharedPreferences.getLong(PREF_LAST_SCAN_TIME, 0L)
    }
    
    /**
     * Updates the last scan time to current time
     */
    private fun updateLastScanTime() {
        sharedPreferences.edit()
            .putLong(PREF_LAST_SCAN_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Gets the status of the SMS monitoring work
     */
    fun getMonitoringWorkStatus(): Flow<SmsMonitoringStatus> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(SmsMonitorWorker.WORK_NAME)
            .map { workInfos ->
                when {
                    workInfos.isEmpty() -> SmsMonitoringStatus.NOT_SCHEDULED
                    workInfos.any { it.state == WorkInfo.State.RUNNING } -> SmsMonitoringStatus.RUNNING
                    workInfos.any { it.state == WorkInfo.State.ENQUEUED } -> SmsMonitoringStatus.SCHEDULED
                    workInfos.any { it.state == WorkInfo.State.SUCCEEDED } -> SmsMonitoringStatus.COMPLETED
                    workInfos.any { it.state == WorkInfo.State.FAILED } -> SmsMonitoringStatus.FAILED
                    else -> SmsMonitoringStatus.UNKNOWN
                }
            }
    }
    
    /**
     * Gets monitoring configuration
     */
    fun getMonitoringConfig(): SmsMonitoringConfig {
        return SmsMonitoringConfig(
            isEnabled = isSmsMonitoringEnabled(),
            intervalHours = getMonitoringIntervalHours(),
            notificationsEnabled = areNotificationsEnabled(),
            lastScanTime = getLastScanTime(),
            hasPermission = permissionManager.hasReadSmsPermission()
        )
    }
    
    /**
     * Applies monitoring configuration
     */
    fun applyMonitoringConfig(config: SmsMonitoringConfig) {
        setNotificationsEnabled(config.notificationsEnabled)
        setMonitoringInterval(config.intervalHours)
        
        if (config.isEnabled && config.hasPermission) {
            enableSmsMonitoring(config.intervalHours)
        } else {
            disableSmsMonitoring()
        }
    }
}

/**
 * Enum representing SMS monitoring status
 */
enum class SmsMonitoringStatus {
    NOT_SCHEDULED,
    SCHEDULED,
    RUNNING,
    COMPLETED,
    FAILED,
    UNKNOWN
}

/**
 * Data class representing SMS monitoring configuration
 */
data class SmsMonitoringConfig(
    val isEnabled: Boolean,
    val intervalHours: Long,
    val notificationsEnabled: Boolean,
    val lastScanTime: Long,
    val hasPermission: Boolean
) {
    val isConfigured: Boolean get() = isEnabled && hasPermission
    val needsPermission: Boolean get() = isEnabled && !hasPermission
}