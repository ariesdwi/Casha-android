package com.casha.app.data.remote.impl

import com.casha.app.data.local.dao.TransactionDao
import com.casha.app.data.local.entity.TransactionEntity
import com.casha.app.data.remote.api.TransactionApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val apiService: TransactionApiService,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun getTransactions(): Flow<List<TransactionCasha>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun fetchTransactions() {
        try {
            val response = apiService.getTransactions()
            val entities = response.transactions.map { it.toEntity() }
            transactionDao.insertTransactions(entities)
        } catch (e: Exception) {
            // Log or handle fetch error
        }
    }

    override suspend fun getTransaction(id: String): TransactionCasha? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun getTotalSpending(period: SpendingPeriod): Double {
        val startDate = getStartDateForPeriod(period)
        return transactionDao.getTotalSpendingSince(startDate.time)
    }

    override suspend fun fetchSpendingReport(): SpendingReport {
        val calendar = Calendar.getInstance()

        // ── Start of current month ──
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // ── Start of current week (Sunday) ──
        val startOfWeek = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Fetch all transactions from start of month
        val allMonthTransactions = transactionDao.getTransactionsSince(startOfMonth.timeInMillis)

        // ── Daily Bars (Mon–Sun for current week) ──
        val weekTransactions = allMonthTransactions.filter {
            it.datetime.time >= startOfWeek.timeInMillis
        }

        val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dailyGrouped = weekTransactions.groupBy { txn ->
            val txnCal = Calendar.getInstance().apply { time = txn.datetime }
            // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ..., Saturday=7
            // Convert to Sunday=0, Monday=1, ..., Saturday=6
            val dow = txnCal.get(Calendar.DAY_OF_WEEK)
            dow - Calendar.SUNDAY // Sunday=0, Monday=1, ..., Saturday=6
        }

        val dailyBars = dayLabels.mapIndexed { index, label ->
            val total = dailyGrouped[index]?.sumOf { it.amount } ?: 0.0
            ChartBar(label = label, value = total)
        }

        // ── Weekly Bars (Week 1–4 for current month) ──
        val weeklyGrouped = allMonthTransactions.groupBy { txn ->
            val txnCal = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.SUNDAY
                time = txn.datetime
            }
            txnCal.get(Calendar.WEEK_OF_MONTH).coerceIn(1, 4)
        }

        val weeklyBars = (1..4).map { weekNum ->
            val total = weeklyGrouped[weekNum]?.sumOf { it.amount } ?: 0.0
            ChartBar(label = "Week $weekNum", value = total)
        }

        val thisWeekTotal = dailyBars.sumOf { it.value }
        val thisMonthTotal = weeklyBars.sumOf { it.value }

        return SpendingReport(
            thisWeekTotal = thisWeekTotal,
            thisMonthTotal = thisMonthTotal,
            dailyBars = dailyBars,
            weeklyBars = weeklyBars
        )
    }

    private fun getStartDateForPeriod(period: SpendingPeriod): Date {
        val calendar = Calendar.getInstance()
        return when (period) {
            SpendingPeriod.THIS_WEEK -> {
                calendar.firstDayOfWeek = Calendar.SUNDAY
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }
            SpendingPeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }
            SpendingPeriod.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }
            SpendingPeriod.LAST_THREE_MONTHS -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }
            SpendingPeriod.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }
            SpendingPeriod.ALL_TIME -> Date(0) // epoch
            SpendingPeriod.FUTURE -> Date() // now
            is SpendingPeriod.CUSTOM -> period.start
        }
    }

    override suspend fun getUnsyncCount(): Int {
        return transactionDao.getUnsyncedTransactions().size
    }

    override suspend fun saveTransaction(transaction: TransactionCasha) {
        // Local first
        transactionDao.insertTransaction(transaction.toEntity())
        
        // Background sync (simplified for now)
        try {
            syncTransactions()
        } catch (e: Exception) {
            // Log sync error, keep it unsynced locally
        }
    }

    override suspend fun deleteTransaction(id: String) {
        val local = transactionDao.getTransactionById(id)
        if (local != null) {
            transactionDao.deleteTransaction(local)
            try {
                apiService.deleteTransaction(id)
            } catch (e: Exception) {
                // If remote delete fails, we might need a "deleted_locally" flag for sync
            }
        }
    }

    override suspend fun syncTransactions() {
        val unsynced = transactionDao.getUnsyncedTransactions()
        unsynced.forEach { entity ->
            try {
                val dto = apiService.createTransaction(entity.toUploadDto())
                // Update local with remote ID (if different) and mark as synced
                transactionDao.insertTransaction(entity.copy(isSynced = true))
            } catch (e: Exception) {
                // Keep unsynced
            }
        }
    }

    // ── Mappers ──

    private fun TransactionEntity.toDomain() = TransactionCasha(
        id = id,
        name = name,
        category = category,
        amount = amount,
        datetime = datetime,
        note = note,
        isSynced = isSynced,
        remoteId = remoteId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TransactionCasha.toEntity() = TransactionEntity(
        id = id,
        name = name,
        category = category,
        amount = amount,
        datetime = datetime,
        note = note,
        isSynced = isSynced,
        remoteId = remoteId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TransactionEntity.toUploadDto() = TransactionUploadDto(
        name = name,
        category = category,
        amount = amount,
        datetime = dateFormat.format(datetime),
        note = note
    )

    private fun TransactionDto.toEntity() = TransactionEntity(
        id = id,
        name = name,
        category = category,
        amount = amount,
        datetime = try { dateFormat.parse(datetime) ?: Date() } catch (e: Exception) { Date() },
        note = note,
        isSynced = true,
        remoteId = id,
        createdAt = try { createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
        updatedAt = try { updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() }
    )
}
