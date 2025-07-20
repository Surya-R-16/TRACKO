package com.expensetracker.data.sms.util

import com.expensetracker.data.sms.model.ParsedTransaction
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class BankSpecificParserTest {
    
    private val testDate = Date()
    
    @Test
    fun `parseHdfcSms parses debit card transaction correctly`() {
        val smsContent = "Rs.500 debited from account ending 1234 at ZOMATO on 15-Jan-24"
        
        val result = BankSpecificParser.parseHdfcSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.01)
        assertEquals("ZOMATO", result.merchantName)
        assertEquals(ParsedTransaction.METHOD_DEBIT_CARD, result.paymentMethod)
        assertEquals("HDFC", result.sender)
        assertEquals(0.9f, result.confidence, 0.01f)
    }
    
    @Test
    fun `parseHdfcSms parses UPI transaction correctly`() {
        val smsContent = "₹150 debited from account via UPI UPI Ref: 123456789"
        
        val result = BankSpecificParser.parseHdfcSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(150.0, result!!.amount, 0.01)
        assertEquals("123456789", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertEquals("HDFC", result.sender)
    }
    
    @Test
    fun `parseHdfcSms parses credit card transaction correctly`() {
        val smsContent = "INR 2000 spent on AMAZON using HDFC Credit Card ending 5678"
        
        val result = BankSpecificParser.parseHdfcSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(2000.0, result!!.amount, 0.01)
        assertEquals("AMAZON", result.merchantName)
        assertEquals(ParsedTransaction.METHOD_CREDIT_CARD, result.paymentMethod)
        assertEquals("HDFC", result.sender)
    }
    
    @Test
    fun `parseHdfcSms returns null for invalid format`() {
        val smsContent = "Your HDFC account balance is Rs.5000"
        
        val result = BankSpecificParser.parseHdfcSms(smsContent, testDate)
        
        assertNull(result)
    }
    
    @Test
    fun `parseSbiSms parses debit transaction correctly`() {
        val smsContent = "Rs 300 debited from account on 15-Jan-24 at SWIGGY"
        
        val result = BankSpecificParser.parseSbiSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(300.0, result!!.amount, 0.01)
        assertEquals("SWIGGY", result.merchantName)
        assertEquals(ParsedTransaction.METHOD_DEBIT_CARD, result.paymentMethod)
        assertEquals("SBI", result.sender)
        assertEquals(0.9f, result.confidence, 0.01f)
    }
    
    @Test
    fun `parseSbiSms parses UPI transaction correctly`() {
        val smsContent = "₹250 sent via UPI to user@paytm Ref: SBI123456"
        
        val result = BankSpecificParser.parseSbiSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(250.0, result!!.amount, 0.01)
        assertEquals("user@paytm", result.recipient)
        assertEquals("SBI123456", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertEquals("SBI", result.sender)
    }
    
    @Test
    fun `parseIciciSms parses debit card transaction correctly`() {
        val smsContent = "₹750 debited from account ending 9876 at UBER on 15-Jan-24"
        
        val result = BankSpecificParser.parseIciciSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(750.0, result!!.amount, 0.01)
        assertEquals("UBER", result.merchantName)
        assertEquals(ParsedTransaction.METHOD_DEBIT_CARD, result.paymentMethod)
        assertEquals("ICICI", result.sender)
        assertEquals(0.9f, result.confidence, 0.01f)
    }
    
    @Test
    fun `parseIciciSms parses UPI transaction correctly`() {
        val smsContent = "₹400 transferred to merchant@ybl via UPI Ref: ICICI789123"
        
        val result = BankSpecificParser.parseIciciSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(400.0, result!!.amount, 0.01)
        assertEquals("merchant@ybl", result.recipient)
        assertEquals("ICICI789123", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertEquals("ICICI", result.sender)
    }
    
    @Test
    fun `parseGpaySms parses standard payment correctly`() {
        val smsContent = "₹180 paid to 9876543210 via UPI UPI Ref: GPAY123456789"
        
        val result = BankSpecificParser.parseGpaySms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(180.0, result!!.amount, 0.01)
        assertEquals("9876543210", result.recipient)
        assertEquals("GPAY123456789", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertEquals("GPAY", result.sender)
        assertEquals(0.95f, result.confidence, 0.01f)
    }
    
    @Test
    fun `parseGpaySms parses merchant payment correctly`() {
        val smsContent = "You paid ₹220 to ZOMATO UPI Ref: GPAY987654321"
        
        val result = BankSpecificParser.parseGpaySms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(220.0, result!!.amount, 0.01)
        assertEquals("ZOMATO", result.merchantName)
        assertEquals("GPAY987654321", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertEquals("GPAY", result.sender)
    }
    
    @Test
    fun `parsePhonePeSms parses standard payment correctly`() {
        val smsContent = "₹320 sent to user@phonepe via PhonePe UPI ID: PHONEPE123456"
        
        val result = BankSpecificParser.parsePhonePeSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(320.0, result!!.amount, 0.01)
        assertEquals("user@phonepe", result.recipient)
        assertEquals("PHONEPE123456", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertEquals("PHONEPE", result.sender)
        assertEquals(0.95f, result.confidence, 0.01f)
    }
    
    @Test
    fun `parsePhonePeSms parses merchant payment correctly`() {
        val smsContent = "You sent ₹450 to OLA Transaction ID: PHONEPE789123"
        
        val result = BankSpecificParser.parsePhonePeSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(450.0, result!!.amount, 0.01)
        assertEquals("OLA", result.merchantName)
        assertEquals("PHONEPE789123", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertEquals("PHONEPE", result.sender)
    }
    
    @Test
    fun `parsePaytmSms parses UPI payment correctly`() {
        val smsContent = "₹280 transferred to merchant@paytm via Paytm UPI Txn ID: PAYTM123456"
        
        val result = BankSpecificParser.parsePaytmSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(280.0, result!!.amount, 0.01)
        assertEquals("merchant@paytm", result.recipient)
        assertEquals("PAYTM123456", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertEquals("PAYTM", result.sender)
        assertEquals(0.95f, result.confidence, 0.01f)
    }
    
    @Test
    fun `parsePaytmSms parses wallet payment correctly`() {
        val smsContent = "₹150 paid from Paytm Wallet to BOOKMYSHOW Order ID: PAYTM789123"
        
        val result = BankSpecificParser.parsePaytmSms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(150.0, result!!.amount, 0.01)
        assertEquals("BOOKMYSHOW", result.merchantName)
        assertEquals("PAYTM789123", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_WALLET, result.paymentMethod)
        assertEquals("PAYTM", result.sender)
    }
    
    @Test
    fun `bank specific parsers return null for invalid formats`() {
        val invalidSms = "Your OTP is 123456"
        
        assertNull(BankSpecificParser.parseHdfcSms(invalidSms, testDate))
        assertNull(BankSpecificParser.parseSbiSms(invalidSms, testDate))
        assertNull(BankSpecificParser.parseIciciSms(invalidSms, testDate))
        assertNull(BankSpecificParser.parseGpaySms(invalidSms, testDate))
        assertNull(BankSpecificParser.parsePhonePeSms(invalidSms, testDate))
        assertNull(BankSpecificParser.parsePaytmSms(invalidSms, testDate))
    }
    
    @Test
    fun `bank specific parsers handle amount with commas correctly`() {
        val smsContent = "₹1,500.50 paid to AMAZON via UPI UPI Ref: TEST123"
        
        val result = BankSpecificParser.parseGpaySms(smsContent, testDate)
        
        assertNotNull(result)
        assertEquals(1500.50, result!!.amount, 0.01)
    }
    
    @Test
    fun `bank specific parsers distinguish between UPI ID and merchant name`() {
        // UPI ID case
        val upiSms = "₹100 paid to user@paytm via UPI UPI Ref: TEST123"
        val upiResult = BankSpecificParser.parseGpaySms(upiSms, testDate)
        
        assertNotNull(upiResult)
        assertEquals("user@paytm", upiResult!!.recipient)
        assertNull(upiResult.merchantName)
        
        // Merchant name case
        val merchantSms = "You paid ₹100 to ZOMATO UPI Ref: TEST456"
        val merchantResult = BankSpecificParser.parseGpaySms(merchantSms, testDate)
        
        assertNotNull(merchantResult)
        assertNull(merchantResult!!.recipient)
        assertEquals("ZOMATO", merchantResult.merchantName)
    }
}