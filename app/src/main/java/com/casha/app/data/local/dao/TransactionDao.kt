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

    @Query("SELECT * FROM transactions WHERE groupId = :groupId ORDER BY datetime DESC")
    suspend fun getTransactionsByGroupId(groupId: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE isSynced = 1 AND id NOT IN (:remoteIds)")
    suspend fun deleteSyncedTransactionsNotIn(remoteIds: List<String>)

    // ── Spending Analytics ──

    @Query("SELECT * FROM transactions WHERE datetime >= :startDate ORDER BY datetime ASC")
    suspend fun getTransactionsSince(startDate: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE datetime >= :startDate AND datetime <= :endDate ORDER BY datetime DESC")
    suspend fun getTransactionsBetween(startDate: Long, endDate: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE category = :category AND datetime >= :startDate AND datetime <= :endDate ORDER BY datetime DESC")
    suspend fun getTransactionsByCategoryBetween(category: String, startDate: Long, endDate: Long): List<TransactionEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE datetime >= :startDate")
    suspend fun getTotalSpendingSince(startDate: Long): Double

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE datetime >= :startDate AND datetime <= :endDate GROUP BY category ORDER BY total DESC")
    suspend fun getCategorySpendingBetween(startDate: Long, endDate: Long): List<CategoryTotal>

    @Query("SELECT strftime('%Y-%m-%d', datetime / 1000, 'unixepoch') as date, SUM(amount) as total FROM transactions WHERE datetime >= :startDate AND datetime <= :endDate GROUP BY date ORDER BY date ASC")
    suspend fun getDailySpendingBetween(startDate: Long, endDate: Long): List<DailyTotal>

    @Query("SELECT strftime('%Y-%m', datetime / 1000, 'unixepoch') as month, SUM(amount) as total FROM transactions WHERE datetime >= :startDate AND datetime <= :endDate GROUP BY month ORDER BY month ASC")
    suspend fun getMonthlySpendingBetween(startDate: Long, endDate: Long): List<MonthlyTotal>

    @Query("SELECT * FROM transactions WHERE datetime >= :startOfDayMs AND datetime < :endOfDayMs ORDER BY datetime DESC")
    suspend fun getTransactionsByDate(startOfDayMs: Long, endOfDayMs: Long): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("DELETE FROM transactions WHERE isSynced = 1")
    suspend fun clearAllSynced()
}

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class DailyTotal(
    val date: String,   // "yyyy-MM-dd"
    val total: Double
)

data class MonthlyTotal(
    val month: String,  // "yyyy-MM"
    val total: Double
)
