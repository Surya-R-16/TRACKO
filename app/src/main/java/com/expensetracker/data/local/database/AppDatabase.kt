package com.expensetracker.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.expensetracker.data.local.converter.DateConverter
import com.expensetracker.data.local.dao.CategoryDao
import com.expensetracker.data.local.dao.TransactionDao
import com.expensetracker.data.local.entity.Category
import com.expensetracker.data.local.entity.Transaction

@Database(
    entities = [Transaction::class, Category::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        const val DATABASE_NAME = "expense_tracker_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Database callback to populate default categories on first creation
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert default categories when database is created
                insertDefaultCategories(db)
            }
            
            private fun insertDefaultCategories(db: SupportSQLiteDatabase) {
                // Insert default categories
                val defaultCategories = listOf(
                    "('Food & Dining', '#FF5722', 'restaurant', 1)",
                    "('Transportation', '#2196F3', 'directions_car', 1)",
                    "('Shopping', '#E91E63', 'shopping_bag', 1)",
                    "('Entertainment', '#9C27B0', 'movie', 1)",
                    "('Bills & Utilities', '#FF9800', 'receipt', 1)",
                    "('Health & Medical', '#4CAF50', 'local_hospital', 1)",
                    "('Education', '#3F51B5', 'school', 1)",
                    "('Personal Care', '#00BCD4', 'face', 1)",
                    "('Other', '#607D8B', 'category', 1)"
                )
                
                defaultCategories.forEach { categoryValues ->
                    db.execSQL("""
                        INSERT INTO categories (name, color, icon, is_default, created_at) 
                        VALUES $categoryValues, ${System.currentTimeMillis()}
                    """.trimIndent())
                }
            }
        }
    }
}

/**
 * Migration from version 1 to 2 (example for future use)
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example migration - add new column to transactions table
        // database.execSQL("ALTER TABLE transactions ADD COLUMN new_column TEXT")
    }
}