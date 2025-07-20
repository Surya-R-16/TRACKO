package com.expensetracker.presentation.transaction

import com.expensetracker.domain.model.Transaction
import java.util.*

/**
 * Helper object to create test transaction data for development
 */
object TestDataHelper {
    
    fun createSampleTransactions(): List<Transaction> {
        val now = Date()
        val calendar = Calendar.getInstance()
        
        return listOf(
            Transaction(
                id = 1,
                amount = 250.50,
                recipient = "john@paytm",
                merchantName = "McDonald's",
                dateTime = now,
                transactionId = "TXN123456789",
                paymentMethod = "UPI",
                category = "Food & Dining",
                notes = null,
                isCategorized = true,
                smsContent = "You have paid Rs.250.50 to john@paytm at McDonald's via UPI. Ref: TXN123456789"
            ),
            Transaction(
                id = 2,
                amount = 1200.00,
                recipient = null,
                merchantName = "Amazon",
                dateTime = Date(now.time - 86400000), // Yesterday
                transactionId = "TXN987654321",
                paymentMethod = "Debit Card",
                category = null,
                notes = null,
                isCategorized = false,
                smsContent = "Your card ending 1234 used for Rs.1200.00 at Amazon. Ref: TXN987654321"
            ),
            Transaction(
                id = 3,
                amount = 45.00,
                recipient = "9876543210",
                merchantName = null,
                dateTime = Date(now.time - 172800000), // 2 days ago
                transactionId = "UPI456789123",
                paymentMethod = "UPI",
                category = null,
                notes = null,
                isCategorized = false,
                smsContent = "UPI payment of Rs.45.00 to 9876543210 successful. UPI Ref: UPI456789123"
            ),
            Transaction(
                id = 4,
                amount = 850.75,
                recipient = null,
                merchantName = "Swiggy",
                dateTime = Date(now.time - 259200000), // 3 days ago
                transactionId = "SWGY789456123",
                paymentMethod = "Credit Card",
                category = "Food & Dining",
                notes = "Dinner order",
                isCategorized = true,
                smsContent = "Credit card payment of Rs.850.75 at Swiggy successful. Ref: SWGY789456123"
            ),
            Transaction(
                id = 5,
                amount = 2500.00,
                recipient = null,
                merchantName = "Reliance Digital",
                dateTime = Date(now.time - 345600000), // 4 days ago
                transactionId = "RD123789456",
                paymentMethod = "Net Banking",
                category = null,
                notes = null,
                isCategorized = false,
                smsContent = "Net banking payment of Rs.2500.00 at Reliance Digital. Transaction ID: RD123789456"
            )
        )
    }
}