package com.expensetracker.data.sms.model

import java.util.Date

/**
 * Data class representing an SMS message
 */
data class SmsMessage(
    val id: Long,
    val address: String, // Sender's phone number or name
    val body: String,    // SMS content
    val date: Date,      // When the SMS was received
    val type: Int,       // SMS type (inbox, sent, etc.)
    val read: Int        // Read status (0 = unread, 1 = read)
) {
    companion object {
        // SMS types
        const val TYPE_INBOX = 1
        const val TYPE_SENT = 2
        const val TYPE_DRAFT = 3
        const val TYPE_OUTBOX = 4
        const val TYPE_FAILED = 5
        const val TYPE_QUEUED = 6
        
        // Read status
        const val STATUS_UNREAD = 0
        const val STATUS_READ = 1
    }
    
    /**
     * Returns true if this SMS is from inbox
     */
    fun isInboxMessage(): Boolean = type == TYPE_INBOX
    
    /**
     * Returns true if this SMS is unread
     */
    fun isUnread(): Boolean = read == STATUS_UNREAD
    
    /**
     * Returns a normalized sender address (uppercase, trimmed)
     */
    fun getNormalizedSender(): String = address.trim().uppercase()
}