package com.expensetracker.data.sms

import com.expensetracker.data.sms.model.SmsMessage

/**
 * Interface for accessing SMS content provider
 */
interface SmsContentProvider {
    
    /**
     * Reads all SMS messages from inbox
     */
    suspend fun getAllSmsMessages(): List<SmsMessage>
    
    /**
     * Reads SMS messages from specific senders
     */
    suspend fun getSmsMessagesFromSenders(senders: List<String>): List<SmsMessage>
    
    /**
     * Reads SMS messages within a date range
     */
    suspend fun getSmsMessagesByDateRange(startDate: Long, endDate: Long): List<SmsMessage>
    
    /**
     * Reads SMS messages from financial institutions
     */
    suspend fun getFinancialSmsMessages(): List<SmsMessage>
    
    /**
     * Reads recent SMS messages (last N messages)
     */
    suspend fun getRecentSmsMessages(limit: Int = 100): List<SmsMessage>
    
    /**
     * Checks if SMS read permission is granted
     */
    fun hasReadSmsPermission(): Boolean
}