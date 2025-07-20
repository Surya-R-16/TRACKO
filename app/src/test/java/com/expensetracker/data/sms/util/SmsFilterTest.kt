package com.expensetracker.data.sms.util

import com.expensetracker.data.sms.model.SmsMessage
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class SmsFilterTest {
    
    @Test
    fun `isTransactionSms returns true for UPI payment messages`() {
        val smsMessage = createSmsMessage(
            body = "₹150 paid to 9876543210 via UPI. UPI Ref: 123456789",
            address = "GPAY"
        )
        
        assertTrue(SmsFilter.isTransactionSms(smsMessage))
    }
    
    @Test
    fun `isTransactionSms returns true for debit card messages`() {
        val smsMessage = createSmsMessage(
            body = "Rs.500 debited from account ending 1234 at ZOMATO on 15-Jan-24",
            address = "HDFCBK"
        )
        
        assertTrue(SmsFilter.isTransactionSms(smsMessage))
    }
    
    @Test
    fun `isTransactionSms returns true for credit card messages`() {
        val smsMessage = createSmsMessage(
            body = "INR 2000 spent on AMAZON using HDFC Credit Card",
            address = "HDFC"
        )
        
        assertTrue(SmsFilter.isTransactionSms(smsMessage))
    }
    
    @Test
    fun `isTransactionSms returns false for non-transaction messages`() {
        val smsMessage = createSmsMessage(
            body = "Your OTP for login is 123456. Do not share with anyone.",
            address = "AMAZON"
        )
        
        assertFalse(SmsFilter.isTransactionSms(smsMessage))
    }
    
    @Test
    fun `isTransactionSms returns false for promotional messages`() {
        val smsMessage = createSmsMessage(
            body = "Get 50% off on your next order. Use code SAVE50",
            address = "ZOMATO"
        )
        
        assertFalse(SmsFilter.isTransactionSms(smsMessage))
    }
    
    @Test
    fun `isFinancialSender returns true for known bank senders`() {
        assertTrue(SmsFilter.isFinancialSender("HDFCBK"))
        assertTrue(SmsFilter.isFinancialSender("SBI"))
        assertTrue(SmsFilter.isFinancialSender("ICICI"))
        assertTrue(SmsFilter.isFinancialSender("AXIS"))
    }
    
    @Test
    fun `isFinancialSender returns true for payment app senders`() {
        assertTrue(SmsFilter.isFinancialSender("GPAY"))
        assertTrue(SmsFilter.isFinancialSender("PHONEPE"))
        assertTrue(SmsFilter.isFinancialSender("PAYTM"))
        assertTrue(SmsFilter.isFinancialSender("AMAZONPAY"))
    }
    
    @Test
    fun `isFinancialSender returns false for unknown senders`() {
        assertFalse(SmsFilter.isFinancialSender("UNKNOWN"))
        assertFalse(SmsFilter.isFinancialSender("RANDOM"))
        assertFalse(SmsFilter.isFinancialSender("9876543210"))
    }
    
    @Test
    fun `filterTransactionSms filters correctly`() {
        val messages = listOf(
            createSmsMessage(
                body = "₹100 paid to ZOMATO via UPI",
                address = "GPAY"
            ),
            createSmsMessage(
                body = "Your OTP is 123456",
                address = "AMAZON"
            ),
            createSmsMessage(
                body = "Rs.200 debited from account",
                address = "HDFC"
            ),
            createSmsMessage(
                body = "Happy Birthday! Enjoy your day",
                address = "9876543210"
            )
        )
        
        val transactionMessages = SmsFilter.filterTransactionSms(messages)
        assertEquals(2, transactionMessages.size)
        assertTrue(transactionMessages.any { it.body.contains("₹100 paid") })
        assertTrue(transactionMessages.any { it.body.contains("Rs.200 debited") })
    }
    
    @Test
    fun `filterByDateRange filters messages correctly`() {
        val baseTime = System.currentTimeMillis()
        val messages = listOf(
            createSmsMessage(date = Date(baseTime - 2 * 24 * 60 * 60 * 1000)), // 2 days ago
            createSmsMessage(date = Date(baseTime - 1 * 24 * 60 * 60 * 1000)), // 1 day ago
            createSmsMessage(date = Date(baseTime)), // now
            createSmsMessage(date = Date(baseTime + 1 * 24 * 60 * 60 * 1000))  // 1 day future
        )
        
        val startTime = baseTime - 1.5 * 24 * 60 * 60 * 1000 // 1.5 days ago
        val endTime = baseTime + 0.5 * 24 * 60 * 60 * 1000   // 0.5 days future
        
        val filteredMessages = SmsFilter.filterByDateRange(messages, startTime.toLong(), endTime.toLong())
        assertEquals(2, filteredMessages.size) // Should include 1 day ago and now
    }
    
    @Test
    fun `filterBySenders filters messages correctly`() {
        val messages = listOf(
            createSmsMessage(address = "HDFC"),
            createSmsMessage(address = "GPAY"),
            createSmsMessage(address = "UNKNOWN"),
            createSmsMessage(address = "PAYTM")
        )
        
        val senders = listOf("HDFC", "GPAY")
        val filteredMessages = SmsFilter.filterBySenders(messages, senders)
        
        assertEquals(2, filteredMessages.size)
        assertTrue(filteredMessages.any { it.address == "HDFC" })
        assertTrue(filteredMessages.any { it.address == "GPAY" })
    }
    
    @Test
    fun `removeDuplicates removes duplicate messages`() {
        val baseTime = System.currentTimeMillis()
        val messages = listOf(
            createSmsMessage(
                body = "₹100 paid to ZOMATO",
                address = "GPAY",
                date = Date(baseTime)
            ),
            createSmsMessage(
                body = "₹100 paid to ZOMATO",
                address = "GPAY",
                date = Date(baseTime + 2 * 60 * 1000) // 2 minutes later
            ),
            createSmsMessage(
                body = "₹200 paid to SWIGGY",
                address = "PHONEPE",
                date = Date(baseTime)
            )
        )
        
        val uniqueMessages = SmsFilter.removeDuplicates(messages)
        assertEquals(2, uniqueMessages.size) // Should remove one duplicate
    }
    
    @Test
    fun `sortByDateDesc sorts messages correctly`() {
        val baseTime = System.currentTimeMillis()
        val messages = listOf(
            createSmsMessage(date = Date(baseTime - 2 * 60 * 60 * 1000)), // 2 hours ago
            createSmsMessage(date = Date(baseTime)), // now
            createSmsMessage(date = Date(baseTime - 1 * 60 * 60 * 1000))  // 1 hour ago
        )
        
        val sortedMessages = SmsFilter.sortByDateDesc(messages)
        
        assertTrue(sortedMessages[0].date.time >= sortedMessages[1].date.time)
        assertTrue(sortedMessages[1].date.time >= sortedMessages[2].date.time)
    }
    
    private fun createSmsMessage(
        id: Long = 1,
        address: String = "TEST",
        body: String = "Test message",
        date: Date = Date(),
        type: Int = SmsMessage.TYPE_INBOX,
        read: Int = SmsMessage.STATUS_READ
    ): SmsMessage {
        return SmsMessage(
            id = id,
            address = address,
            body = body,
            date = date,
            type = type,
            read = read
        )
    }
}