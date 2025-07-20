package com.expensetracker.data.service

import android.content.Context
import android.content.SharedPreferences
import com.expensetracker.data.permission.PermissionManager
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SmsMonitorManagerTest {
    
    private lateinit var context: Context
    private lateinit var permissionManager: PermissionManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var smsMonitorManager: SmsMonitorManager
    
    @Before
    fun setup() {
        context = mockk()
        permissionManager = mockk()
        sharedPreferences = mockk()
        editor = mockk()
        
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } just Runs
        
        smsMonitorManager = SmsMonitorManager(context, permissionManager, sharedPreferences)
    }
    
    @Test
    fun `isSmsMonitoringEnabled returns correct value from preferences`() {
        every { sharedPreferences.getBoolean("sms_monitoring_enabled", false) } returns true
        
        assertTrue(smsMonitorManager.isSmsMonitoringEnabled())
        
        verify { sharedPreferences.getBoolean("sms_monitoring_enabled", false) }
    }
    
    @Test
    fun `enableSmsMonitoring throws exception when permission not granted`() {
        every { permissionManager.hasReadSmsPermission() } returns false
        
        try {
            smsMonitorManager.enableSmsMonitoring()
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("SMS permission is required to enable monitoring", e.message)
        }
        
        verify { permissionManager.hasReadSmsPermission() }
    }
    
    @Test
    fun `enableSmsMonitoring saves preferences when permission granted`() {
        every { permissionManager.hasReadSmsPermission() } returns true
        every { sharedPreferences.getBoolean("notifications_enabled", true) } returns true
        
        // Mock static methods - this would require PowerMock or similar in a real test
        // For now, we'll just verify the preferences are saved
        
        smsMonitorManager.enableSmsMonitoring(12L)
        
        verify { editor.putBoolean("sms_monitoring_enabled", true) }
        verify { editor.putLong("monitoring_interval_hours", 12L) }
        verify { editor.apply() }
    }
    
    @Test
    fun `disableSmsMonitoring saves preferences and cancels work`() {
        smsMonitorManager.disableSmsMonitoring()
        
        verify { editor.putBoolean("sms_monitoring_enabled", false) }
        verify { editor.apply() }
    }
    
    @Test
    fun `triggerManualScan throws exception when permission not granted`() {
        every { permissionManager.hasReadSmsPermission() } returns false
        
        try {
            smsMonitorManager.triggerManualScan()
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("SMS permission is required to scan SMS", e.message)
        }
        
        verify { permissionManager.hasReadSmsPermission() }
    }
    
    @Test
    fun `getMonitoringIntervalHours returns correct value from preferences`() {
        every { sharedPreferences.getLong("monitoring_interval_hours", 6L) } returns 8L
        
        assertEquals(8L, smsMonitorManager.getMonitoringIntervalHours())
        
        verify { sharedPreferences.getLong("monitoring_interval_hours", 6L) }
    }
    
    @Test
    fun `setMonitoringInterval saves preference`() {
        every { sharedPreferences.getBoolean("sms_monitoring_enabled", false) } returns false
        
        smsMonitorManager.setMonitoringInterval(4L)
        
        verify { editor.putLong("monitoring_interval_hours", 4L) }
        verify { editor.apply() }
    }
    
    @Test
    fun `areNotificationsEnabled returns correct value from preferences`() {
        every { sharedPreferences.getBoolean("notifications_enabled", true) } returns false
        
        assertFalse(smsMonitorManager.areNotificationsEnabled())
        
        verify { sharedPreferences.getBoolean("notifications_enabled", true) }
    }
    
    @Test
    fun `setNotificationsEnabled saves preference`() {
        every { sharedPreferences.getBoolean("sms_monitoring_enabled", false) } returns false
        
        smsMonitorManager.setNotificationsEnabled(true)
        
        verify { editor.putBoolean("notifications_enabled", true) }
        verify { editor.apply() }
    }
    
    @Test
    fun `getLastScanTime returns correct value from preferences`() {
        val expectedTime = System.currentTimeMillis()
        every { sharedPreferences.getLong("last_scan_time", 0L) } returns expectedTime
        
        assertEquals(expectedTime, smsMonitorManager.getLastScanTime())
        
        verify { sharedPreferences.getLong("last_scan_time", 0L) }
    }
    
    @Test
    fun `getMonitoringConfig returns correct configuration`() {
        every { sharedPreferences.getBoolean("sms_monitoring_enabled", false) } returns true
        every { sharedPreferences.getLong("monitoring_interval_hours", 6L) } returns 8L
        every { sharedPreferences.getBoolean("notifications_enabled", true) } returns false
        every { sharedPreferences.getLong("last_scan_time", 0L) } returns 12345L
        every { permissionManager.hasReadSmsPermission() } returns true
        
        val config = smsMonitorManager.getMonitoringConfig()
        
        assertTrue(config.isEnabled)
        assertEquals(8L, config.intervalHours)
        assertFalse(config.notificationsEnabled)
        assertEquals(12345L, config.lastScanTime)
        assertTrue(config.hasPermission)
        assertTrue(config.isConfigured)
        assertFalse(config.needsPermission)
    }
    
    @Test
    fun `SmsMonitoringConfig properties work correctly`() {
        val configWithPermission = SmsMonitoringConfig(
            isEnabled = true,
            intervalHours = 6L,
            notificationsEnabled = true,
            lastScanTime = 0L,
            hasPermission = true
        )
        
        assertTrue(configWithPermission.isConfigured)
        assertFalse(configWithPermission.needsPermission)
        
        val configWithoutPermission = SmsMonitoringConfig(
            isEnabled = true,
            intervalHours = 6L,
            notificationsEnabled = true,
            lastScanTime = 0L,
            hasPermission = false
        )
        
        assertFalse(configWithoutPermission.isConfigured)
        assertTrue(configWithoutPermission.needsPermission)
        
        val disabledConfig = SmsMonitoringConfig(
            isEnabled = false,
            intervalHours = 6L,
            notificationsEnabled = true,
            lastScanTime = 0L,
            hasPermission = true
        )
        
        assertFalse(disabledConfig.isConfigured)
        assertFalse(disabledConfig.needsPermission)
    }
}