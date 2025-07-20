package com.expensetracker.data.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionManagerTest {
    
    private lateinit var context: Context
    private lateinit var permissionManager: PermissionManager
    
    @Before
    fun setup() {
        context = mockk()
        permissionManager = PermissionManager(context)
        mockkStatic(ContextCompat::class)
    }
    
    @After
    fun teardown() {
        unmockkStatic(ContextCompat::class)
    }
    
    @Test
    fun `hasReadSmsPermission returns true when permission is granted`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        } returns PackageManager.PERMISSION_GRANTED
        
        assertTrue(permissionManager.hasReadSmsPermission())
    }
    
    @Test
    fun `hasReadSmsPermission returns false when permission is denied`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        } returns PackageManager.PERMISSION_DENIED
        
        assertFalse(permissionManager.hasReadSmsPermission())
    }
    
    @Test
    fun `hasWriteStoragePermission returns true when permission is granted`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } returns PackageManager.PERMISSION_GRANTED
        
        assertTrue(permissionManager.hasWriteStoragePermission())
    }
    
    @Test
    fun `hasWriteStoragePermission returns false when permission is denied`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } returns PackageManager.PERMISSION_DENIED
        
        assertFalse(permissionManager.hasWriteStoragePermission())
    }
    
    @Test
    fun `getMissingPermissions returns empty list when all permissions granted`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_GRANTED
        
        val missingPermissions = permissionManager.getMissingPermissions()
        assertTrue(missingPermissions.isEmpty())
    }
    
    @Test
    fun `getMissingPermissions returns SMS permission when only SMS is missing`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        } returns PackageManager.PERMISSION_DENIED
        
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } returns PackageManager.PERMISSION_GRANTED
        
        val missingPermissions = permissionManager.getMissingPermissions()
        assertEquals(1, missingPermissions.size)
        assertTrue(missingPermissions.contains(Manifest.permission.READ_SMS))
    }
    
    @Test
    fun `getMissingPermissions returns both permissions when both are missing`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_DENIED
        
        val missingPermissions = permissionManager.getMissingPermissions()
        assertEquals(2, missingPermissions.size)
        assertTrue(missingPermissions.contains(Manifest.permission.READ_SMS))
        assertTrue(missingPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }
    
    @Test
    fun `getRequiredPermissions returns all required permissions`() {
        val requiredPermissions = permissionManager.getRequiredPermissions()
        assertEquals(2, requiredPermissions.size)
        assertTrue(requiredPermissions.contains(Manifest.permission.READ_SMS))
        assertTrue(requiredPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }
    
    @Test
    fun `hasAllRequiredPermissions returns true when all permissions granted`() {
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_GRANTED
        
        assertTrue(permissionManager.hasAllRequiredPermissions())
    }
    
    @Test
    fun `hasAllRequiredPermissions returns false when any permission is missing`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        } returns PackageManager.PERMISSION_DENIED
        
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } returns PackageManager.PERMISSION_GRANTED
        
        assertFalse(permissionManager.hasAllRequiredPermissions())
    }
}