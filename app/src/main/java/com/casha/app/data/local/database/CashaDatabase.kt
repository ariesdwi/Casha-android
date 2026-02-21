package com.casha.app.data.local.database

import androidx.room.*
import com.casha.app.data.local.dao.*
import com.casha.app.data.local.entity.*

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        CategoryEntity::class,
        IncomeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CashaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun incomeDao(): IncomeDao
}
