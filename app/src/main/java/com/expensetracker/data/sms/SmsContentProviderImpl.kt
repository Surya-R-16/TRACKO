package com.expensetracker.data.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.expensetracker.data.sms.model.SmsMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsContentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SmsContentProvider {
    
    companion object {
        // Known financial SMS senders
        private val FINANCIAL_SENDERS = listOf(
            // Banks
            "HDFC", "HDFCBK", "SBI", "SBICARD", "ICICI", "ICICIBK", "AXIS", "AXISBK",
            "KOTAK", "KOTAKBK", "PNB", "PNBBK", "BOB", "BOBBK", "CANARA", "CANBK",
            "UNION", "UNIONBK", "INDIAN", "INDBK", "FEDERAL", "FEDBK",
            
            // Payment Apps
            "GPAY", "GOOGLEPAY", "PHONEPE", "PAYTM", "AMAZONPAY", "MOBIKWIK",
            "FREECHARGE", "PAYPAL", "BHIM", "WHATSAPP",
            
            // Credit Cards
            "AMEX", "CITI", "CITIBANK", "HSBC", "STANCHART", "YESBANK",
            
            // Wallets & Others
            "OLAMONEY", "JIOMONEY", "AIRTEL", "VODAFONE", "JIO",
            
            // E-commerce with payments
            "AMAZON", "FLIPKART", "MYNTRA", "ZOMATO", "SWIGGY", "UBER", "OLA"
        )
        
        // SMS content projection
        private val SMS_PROJECTION = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )
    }
    
    override suspend fun getAllSmsMessages(): List<SmsMessage> = withContext(Dispatchers.IO) {
        if (!hasReadSmsPermission()) {
            return@withContext emptyList()
        }
        
        return@withContext querySmsMessages(
            selection = "${Telephony.Sms.TYPE} = ?",
            selectionArgs = arrayOf(SmsMessage.TYPE_INBOX.toString()),
            sortOrder = "${Telephony.Sms.DATE} DESC"
        )
    }
    
    override suspend fun getSmsMessagesFromSenders(senders: List<String>): List<SmsMessage> = withContext(Dispatchers.IO) {
        if (!hasReadSmsPermission() || senders.isEmpty()) {
            return@withContext emptyList()
        }
        
        val placeholders = senders.joinToString(",") { "?" }
        val selection = "${Telephony.Sms.TYPE} = ? AND ${Telephony.Sms.ADDRESS} IN ($placeholders)"
        val selectionArgs = arrayOf(SmsMessage.TYPE_INBOX.toString()) + senders.toTypedArray()
        
        return@withContext querySmsMessages(
            selection = selection,
            selectionArgs = selectionArgs,
            sortOrder = "${Telephony.Sms.DATE} DESC"
        )
    }
    
    override suspend fun getSmsMessagesByDateRange(startDate: Long, endDate: Long): List<SmsMessage> = withContext(Dispatchers.IO) {
        if (!hasReadSmsPermission()) {
            return@withContext emptyList()
        }
        
        val selection = "${Telephony.Sms.TYPE} = ? AND ${Telephony.Sms.DATE} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(
            SmsMessage.TYPE_INBOX.toString(),
            startDate.toString(),
            endDate.toString()
        )
        
        return@withContext querySmsMessages(
            selection = selection,
            selectionArgs = selectionArgs,
            sortOrder = "${Telephony.Sms.DATE} DESC"
        )
    }
    
    override suspend fun getFinancialSmsMessages(): List<SmsMessage> = withContext(Dispatchers.IO) {
        if (!hasReadSmsPermission()) {
            return@withContext emptyList()
        }
        
        // Create LIKE conditions for each financial sender
        val likeConditions = FINANCIAL_SENDERS.joinToString(" OR ") { 
            "${Telephony.Sms.ADDRESS} LIKE ?" 
        }
        val selection = "${Telephony.Sms.TYPE} = ? AND ($likeConditions)"
        val selectionArgs = arrayOf(SmsMessage.TYPE_INBOX.toString()) + 
                           FINANCIAL_SENDERS.map { "%$it%" }.toTypedArray()
        
        return@withContext querySmsMessages(
            selection = selection,
            selectionArgs = selectionArgs,
            sortOrder = "${Telephony.Sms.DATE} DESC"
        )
    }
    
    override suspend fun getRecentSmsMessages(limit: Int): List<SmsMessage> = withContext(Dispatchers.IO) {
        if (!hasReadSmsPermission()) {
            return@withContext emptyList()
        }
        
        return@withContext querySmsMessages(
            selection = "${Telephony.Sms.TYPE} = ?",
            selectionArgs = arrayOf(SmsMessage.TYPE_INBOX.toString()),
            sortOrder = "${Telephony.Sms.DATE} DESC",
            limit = limit
        )
    }
    
    override fun hasReadSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Generic method to query SMS messages with given parameters
     */
    private fun querySmsMessages(
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null,
        limit: Int? = null
    ): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        
        try {
            val uri = if (limit != null) {
                Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, "inbox?limit=$limit")
            } else {
                Telephony.Sms.Inbox.CONTENT_URI
            }
            
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                SMS_PROJECTION,
                selection,
                selectionArgs,
                sortOrder
            )
            
            cursor?.use { c ->
                val idIndex = c.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val typeIndex = c.getColumnIndexOrThrow(Telephony.Sms.TYPE)
                val readIndex = c.getColumnIndexOrThrow(Telephony.Sms.READ)
                
                while (c.moveToNext()) {
                    val smsMessage = SmsMessage(
                        id = c.getLong(idIndex),
                        address = c.getString(addressIndex) ?: "",
                        body = c.getString(bodyIndex) ?: "",
                        date = Date(c.getLong(dateIndex)),
                        type = c.getInt(typeIndex),
                        read = c.getInt(readIndex)
                    )
                    messages.add(smsMessage)
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
        }
        
        return messages
    }
}