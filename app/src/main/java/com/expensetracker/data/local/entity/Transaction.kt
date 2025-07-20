package com.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date_time"], name = "idx_transaction_date"),
        Index(value = ["category"], name = "idx_transaction_category"),
        Index(value = ["is_categorized"], name = "idx_transaction_categorized"),
        Index(value = ["amount", "date_time"], name = "idx_transaction_amount_date")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "recipient")
    val recipient: String?,
    
    @ColumnInfo(name = "merchant_name")
    val merchantName: String?,
    
    @ColumnInfo(name = "date_time")
    val dateTime: Date,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: String?,
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,
    
    @ColumnInfo(name = "category")
    val category: String?,
    
    @ColumnInfo(name = "notes")
    val notes: String?,
    
    @ColumnInfo(name = "is_categorized")
    val isCategorized: Boolean = false,
    
    @ColumnInfo(name = "sms_content")
    val smsContent: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)