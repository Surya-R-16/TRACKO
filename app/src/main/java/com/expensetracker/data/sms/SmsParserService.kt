package com.expensetracker.data.sms

import com.expensetracker.data.sms.model.ParsedTransaction
import com.expensetracker.data.sms.model.SmsMessage
import com.expensetracker.data.sms.util.DuplicateDetectionSummary
import com.expensetracker.domain.model.Transaction

/**
 * Interface for parsing transaction details from SMS messages
 */
interface SmsParserService {
    
    /**
     * Parses a single SMS message and extracts transaction details
     */
    suspend fun parseTransactionSms(smsMessage: SmsMessage): ParsedTransaction?
    
    /**
     * Parses multiple SMS messages and returns list of parsed transactions
     */
    suspend fun parseTransactionSmsMessages(smsMessages: List<SmsMessage>): List<ParsedTransaction>
    
    /**
     * Parses SMS messages and removes duplicates based on existing transactions
     */
    suspend fun parseAndFilterDuplicates(
        smsMessages: List<SmsMessage>,
        existingTransactions: List<Transaction>
    ): DuplicateDetectionSummary
    
    /**
     * Checks if an SMS message can be parsed as a transaction
     */
    fun canParseTransaction(smsMessage: SmsMessage): Boolean
    
    /**
     * Extracts amount from SMS content
     */
    fun extractAmount(smsContent: String): Double?
    
    /**
     * Extracts recipient/merchant from SMS content
     */
    fun extractRecipient(smsContent: String): String?
    
    /**
     * Extracts transaction ID from SMS content
     */
    fun extractTransactionId(smsContent: String): String?
    
    /**
     * Determines payment method from SMS content and sender
     */
    fun determinePaymentMethod(smsContent: String, sender: String): String
}