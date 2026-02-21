package com.casha.app.data.local.dao

import androidx.room.*
import com.casha.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY datetime DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // ── Spending Analytics ──

    @Query("SELECT * FROM transactions WHERE datetime >= :startDate ORDER BY datetime ASC")
    suspend fun getTransactionsSince(startDate: Long): List<TransactionEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE datetime >= :startDate")
    suspend fun getTotalSpendingSince(startDate: Long): Double

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
