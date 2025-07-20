package com.expensetracker.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.expensetracker.data.sms.model.SmsMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * Broadcast receiver for incoming SMS messages
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var automaticTransactionProcessor: AutomaticTransactionProcessor
    
    @Inject
    lateinit var smsMonitorManager: SmsMonitorManager
    
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }
        
        // Only process if SMS monitoring is enabled
        if (!smsMonitorManager.isSmsMonitoringEnabled()) {
            return
        }
        
        // Extract SMS messages from intent
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (smsMessages.isNullOrEmpty()) {
            return
        }
        
        // Process each SMS message
        receiverScope.launch {
            for (smsMessage in smsMessages) {
                val customSmsMessage = SmsMessage(
                    id = System.currentTimeMillis(), // Use timestamp as ID for new messages
                    address = smsMessage.originatingAddress ?: "",
                    body = smsMessage.messageBody ?: "",
                    date = Date(smsMessage.timestampMillis),
                    type = SmsMessage.TYPE_INBOX,
                    read = SmsMessage.STATUS_UNREAD
                )
                
                // Process the SMS message
                val result = automaticTransactionProcessor.processSingleSmsMessage(customSmsMessage)
                
                when (result) {
                    is SingleMessageResult.Success -> {
                        // Transaction was successfully processed
                        // The processor will handle notifications
                    }
                    
                    is SingleMessageResult.Duplicate -> {
                        // Duplicate transaction - no action needed
                    }
                    
                    is SingleMessageResult.NotTransactionSms -> {
                        // Not a transaction SMS - no action needed
                    }
                    
                    is SingleMessageResult.ParsingFailed -> {
                        // Failed to parse - could log for debugging
                    }
                    
                    is SingleMessageResult.Error -> {
                        // Error occurred - could log for debugging
                    }
                }
            }
        }
    }
}