package com.expensetracker.data.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling app permissions
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Checks if READ_SMS permission is granted
     */
    fun hasReadSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Checks if WRITE_EXTERNAL_STORAGE permission is granted
     */
    fun hasWriteStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Returns list of required permissions that are not granted
     */
    fun getMissingPermissions(): List<String> {
        val missingPermissions = mutableListOf<String>()
        
        if (!hasReadSmsPermission()) {
            missingPermissions.add(Manifest.permission.READ_SMS)
        }
        
        if (!hasWriteStoragePermission()) {
            missingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        return missingPermissions
    }
    
    /**
     * Returns all required permissions for the app
     */
    fun getRequiredPermissions(): List<String> {
        return listOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    /**
     * Checks if all required permissions are granted
     */
    fun hasAllRequiredPermissions(): Boolean {
        return getMissingPermissions().isEmpty()
    }
}