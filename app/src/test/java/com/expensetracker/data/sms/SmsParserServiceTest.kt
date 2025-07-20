package com.expensetracker.data.sms

import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.data.sms.model.SmsMessage
import com.expensetracker.data.sms.util.DuplicateDetectionService
import com.expensetracker.domain.model.Transaction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class SmsParserServiceTest {
    
    private lateinit var smsParserService: SmsParserService
    
    @Before
    fun setup() {
        val duplicateDetectionService = DuplicateDetectionService()
        smsParserService = SmsParserServiceImpl(duplicateDetectionService)
    }
    
    @Test
    fun `parseTransactionSms parses GPay UPI transaction correctly`() = runTest {
        val smsMessage = createSmsMessage(
            body = "₹150 paid to 9876543210 via UPI. UPI Ref: 123456789",
            address = "GPAY"
        )
        
        val result = smsParserService.parseTransactionSms(smsMessage)
        
        assertNotNull(result)
        assertEquals(150.0, result!!.amount, 0.01)
        assertEquals("9876543210", result.recipient)
        assertEquals("123456789", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertTrue(result.isValid())
        assertTrue(result.isHighConfidence())
    }
    
    @Test
    fun `parseTransactionSms parses HDFC debit card transaction correctly`() = runTest {
        val smsMessage = createSmsMessage(
            body = "Rs.500 debited from account ending 1234 at ZOMATO on 15-Jan-24",
            address = "HDFCBK"
        )
        
        val result = smsParserService.parseTransactionSms(smsMessage)
        
        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.01)
        assertEquals("ZOMATO", result.merchantName)
        assertEquals(ParsedTransaction.METHOD_DEBIT_CARD, result.paymentMethod)
        assertTrue(result.isValid())
    }
    
    @Test
    fun `parseTransactionSms parses PhonePe transaction correctly`() = runTest {
        val smsMessage = createSmsMessage(
            body = "₹200 sent to user@paytm via PhonePe UPI ID: TXN123456",
            address = "PHONEPE"
        )
        
        val result = smsParserService.parseTransactionSms(smsMessage)
        
        assertNotNull(result)
        assertEquals(200.0, result!!.amount, 0.01)
        assertEquals("user@paytm", result.recipient)
        assertEquals("TXN123456", result.transactionId)
        assertEquals(ParsedTransaction.METHOD_UPI, result.paymentMethod)
        assertTrue(result.isValid())
    }
    
    @Test
    fun `parseTransactionSms parses credit card transaction correctly`() = runTest {
        val smsMessage = createSmsMessage(
            body = "INR 2000 spent on AMAZON using HDFC Credit Card ending 5678",
            address = "HDFC"
        )
        
        val result = smsParserService.parseTransactionSms(smsMessage)
        
        assertNotNull(result)
        assertEquals(2000.0, result!!.amount, 0.01)
        assertEquals("AMAZON", result.merchantName)
        assertEquals(ParsedTransaction.METHOD_CREDIT_CARD, result.paymentMethod)
        assertTrue(result.isValid())
    }
    
    @Test
    fun `parseTransactionSms returns null for non-transaction SMS`() = runTest {
        val smsMessage = createSmsMessage(
            body = "Your OTP for login is 123456. Do not share with anyone.",
            address = "AMAZON"
        )
        
        val result = smsParserService.parseTransactionSms(smsMessage)
        
        assertNull(result)
    }
    
    @Test
    fun `parseTransactionSms returns null for SMS without amount`() = runTest {
        val smsMessage = createSmsMessage(
            body = "Payment successful to ZOMATO. Thank you for your order.",
            address = "ZOMATO"
        )
        
        val result = smsParserService.parseTransactionSms(smsMessage)
        
        assertNull(result)
    }
    
    @Test
    fun `extractAmount extracts rupee symbol amounts correctly`() {
        assertEquals(150.0, smsParserService.extractAmount("₹150 paid to merchant"), 0.01)
        assertEquals(1500.50, smsParserService.extractAmount("₹1,500.50 debited from account"), 0.01)
        assertEquals(100.0, smsParserService.extractAmount("Amount: ₹100"), 0.01)
    }
    
    @Test
    fun `extractAmount extracts Rs format amounts correctly`() {
        assertEquals(250.0, smsParserService.extractAmount("Rs.250 spent at store"), 0.01)
        assertEquals(1000.0, smsParserService.extractAmount("Rs 1,000 transferred"), 0.01)
        assertEquals(75.50, smsParserService.extractAmount("rs. 75.50 paid"), 0.01)
    }
    
    @Test
    fun `extractAmount extracts INR format amounts correctly`() {
        assertEquals(500.0, smsParserService.extractAmount("INR 500 charged"), 0.01)
        assertEquals(2500.25, smsParserService.extractAmount("INR 2,500.25 debited"), 0.01)
    }
    
    @Test
    fun `extractAmount returns null for invalid amounts`() {
        assertNull(smsParserService.extractAmount("No amount in this message"))
        assertNull(smsParserService.extractAmount("Invalid ₹ format"))
        assertNull(smsParserService.extractAmount("₹abc invalid"))
    }
    
    @Test
    fun `extractRecipient extracts UPI IDs correctly`() {
        assertEquals("user@paytm", smsParserService.extractRecipient("₹100 paid to user@paytm via UPI"))
        assertEquals("merchant@ybl", smsParserService.extractRecipient("Sent ₹200 to merchant@ybl"))
        assertEquals("test.user@oksbi", smsParserService.extractRecipient("Payment to test.user@oksbi successful"))
    }
    
    @Test
    fun `extractRecipient extracts phone numbers correctly`() {
        assertEquals("9876543210", smsParserService.extractRecipient("₹150 paid to 9876543210"))
        assertEquals("8765432109", smsParserService.extractRecipient("Sent to 8765432109 via UPI"))
        assertEquals("7654321098", smsParserService.extractRecipient("Payment to 7654321098"))
    }
    
    @Test
    fun `extractRecipient returns null when no recipient found`() {
        assertNull(smsParserService.extractRecipient("₹100 debited from account"))
        assertNull(smsParserService.extractRecipient("Balance inquiry successful"))
        assertNull(smsParserService.extractRecipient("Invalid recipient format"))
    }
    
    @Test
    fun `extractTransactionId extracts various reference formats`() {
        assertEquals("123456789", smsParserService.extractTransactionId("UPI Ref: 123456789"))
        assertEquals("TXN987654", smsParserService.extractTransactionId("Transaction ID: TXN987654"))
        assertEquals("REF123ABC", smsParserService.extractTransactionId("Ref: REF123ABC"))
        assertEquals("ORDER456", smsParserService.extractTransactionId("Reference: ORDER456"))
    }
    
    @Test
    fun `extractTransactionId returns null when no ID found`() {
        assertNull(smsParserService.extractTransactionId("No transaction ID in this message"))
        assertNull(smsParserService.extractTransactionId("Invalid ref format"))
    }
    
    @Test
    fun `determinePaymentMethod identifies UPI correctly`() {
        assertEquals(ParsedTransaction.METHOD_UPI, 
            smsParserService.determinePaymentMethod("paid via UPI", "GPAY"))
        assertEquals(ParsedTransaction.METHOD_UPI, 
            smsParserService.determinePaymentMethod("PhonePe payment", "PHONEPE"))
        assertEquals(ParsedTransaction.METHOD_UPI, 
            smsParserService.determinePaymentMethod("BHIM transaction", "BHIM"))
    }
    
    @Test
    fun `determinePaymentMethod identifies card payments correctly`() {
        assertEquals(ParsedTransaction.METHOD_DEBIT_CARD, 
            smsParserService.determinePaymentMethod("debit card transaction", "HDFC"))
        assertEquals(ParsedTransaction.METHOD_CREDIT_CARD, 
            smsParserService.determinePaymentMethod("credit card payment", "ICICI"))
        assertEquals(ParsedTransaction.METHOD_DEBIT_CARD, 
            smsParserService.determinePaymentMethod("card payment", "SBI"))
    }
    
    @Test
    fun `determinePaymentMethod identifies wallet payments correctly`() {
        assertEquals(ParsedTransaction.METHOD_WALLET, 
            smsParserService.determinePaymentMethod("wallet payment", "PAYTM"))
        assertEquals(ParsedTransaction.METHOD_WALLET, 
            smsParserService.determinePaymentMethod("prepaid wallet", "MOBIKWIK"))
    }
    
    @Test
    fun `determinePaymentMethod identifies net banking correctly`() {
        assertEquals(ParsedTransaction.METHOD_NET_BANKING, 
            smsParserService.determinePaymentMethod("net banking transfer", "HDFC"))
        assertEquals(ParsedTransaction.METHOD_NET_BANKING, 
            smsParserService.determinePaymentMethod("online banking", "SBI"))
    }
    
    @Test
    fun `canParseTransaction returns true for valid transaction SMS`() {
        val validSms = createSmsMessage(
            body = "₹100 paid to ZOMATO via UPI",
            address = "GPAY"
        )
        
        assertTrue(smsParserService.canParseTransaction(validSms))
    }
    
    @Test
    fun `canParseTransaction returns false for invalid SMS`() {
        val invalidSms = createSmsMessage(
            body = "Your OTP is 123456",
            address = "BANK"
        )
        
        assertFalse(smsParserService.canParseTransaction(invalidSms))
    }
    
    @Test
    fun `parseTransactionSmsMessages processes multiple SMS correctly`() = runTest {
        val smsMessages = listOf(
            createSmsMessage(
                body = "₹100 paid to ZOMATO via UPI. Ref: TXN123",
                address = "GPAY"
            ),
            createSmsMessage(
                body = "Your OTP is 456789", // Invalid transaction SMS
                address = "BANK"
            ),
            createSmsMessage(
                body = "Rs.200 debited from account at SWIGGY",
                address = "HDFC"
            )
        )
        
        val results = smsParserService.parseTransactionSmsMessages(smsMessages)
        
        assertEquals(2, results.size) // Should parse 2 valid transactions
        assertTrue(results.all { it.isValid() })
        assertTrue(results.any { it.amount == 100.0 })
        assertTrue(results.any { it.amount == 200.0 })
    }
    
    @Test
    fun `parseTransactionSmsMessages removes duplicate transactions`() = runTest {
        val baseTime = Date()
        val smsMessages = listOf(
            createSmsMessage(
                body = "₹150 paid to ZOMATO via UPI. Ref: TXN123",
                address = "GPAY",
                date = baseTime
            ),
            createSmsMessage(
                body = "₹150 paid to ZOMATO via UPI. Ref: TXN456", // Duplicate with different ref
                address = "GPAY",
                date = Date(baseTime.time + 2 * 60 * 1000) // 2 minutes later
            ),
            createSmsMessage(
                body = "Rs.200 debited from account at SWIGGY",
                address = "HDFC",
                date = baseTime
            )
        )
        
        val results = smsParserService.parseTransactionSmsMessages(smsMessages)
        
        assertEquals(2, results.size) // Should remove 1 duplicate, keep 2 unique
        assertTrue(results.any { it.amount == 150.0 && it.merchantName == "ZOMATO" })
        assertTrue(results.any { it.amount == 200.0 && it.merchantName == "SWIGGY" })
    }
    
    @Test
    fun `parseAndFilterDuplicates provides comprehensive duplicate analysis`() = runTest {
        val baseTime = Date()
        val smsMessages = listOf(
            createSmsMessage(
                body = "₹100 paid to ZOMATO via UPI",
                address = "GPAY",
                date = baseTime
            ),
            createSmsMessage(
                body = "₹200 paid to SWIGGY via UPI",
                address = "PHONEPE",
                date = baseTime
            )
        )
        
        val existingTransactions = listOf(
            Transaction(
                amount = 100.0,
                merchantName = "ZOMATO",
                dateTime = Date(baseTime.time + 1 * 60 * 1000), // 1 minute later - duplicate
                paymentMethod = Transaction.PAYMENT_METHOD_UPI,
                smsContent = "Existing ZOMATO transaction"
            )
        )
        
        val summary = smsParserService.parseAndFilterDuplicates(smsMessages, existingTransactions)
        
        assertEquals(2, summary.totalParsed)
        assertEquals(1, summary.uniqueCount) // SWIGGY is unique
        assertEquals(1, summary.duplicateCount) // ZOMATO is duplicate
        assertTrue(summary.potentialDuplicates.isNotEmpty())
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