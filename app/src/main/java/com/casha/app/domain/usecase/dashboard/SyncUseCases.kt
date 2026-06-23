package com.casha.app.domain.usecase.dashboard

import com.casha.app.data.local.dao.IncomeDao
import com.casha.app.data.local.dao.TransactionDao
import com.casha.app.data.local.entity.IncomeEntity
import com.casha.app.data.local.entity.TransactionEntity
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.CashflowRepository
import com.casha.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date
import javax.inject.Inject

/**
 * Responsible for syncing cashflow data from API to local Room DB.
 *
 * Implements the iOS "Cashflow Split" pattern:
 *   API /cashflow/history → split by type → expenses to TransactionEntity, incomes to IncomeEntity
 */
class CashflowSyncUseCase @Inject constructor(
    private val cashflowRepository: CashflowRepository,
    private val transactionDao: TransactionDao,
    private val incomeDao: IncomeDao
) {
    /**
     * Fetches cashflow history from remote, splits by type, and merges into local Room DB.
     * Returns the entries for immediate display.
     */
    suspend fun syncAndFetch(): List<CashflowEntry> {
        try {
            // 1️⃣ Fetch from API via CashflowRepository
            val result = cashflowRepository.getHistory(
                month = null,
                year = null,
                page = 1,
                pageSize = 100
            )

            // 2️⃣ Split entries by type
            val expenses = result.entries.filter { it.type == CashflowType.EXPENSE }
            val incomes = result.entries.filter { it.type == CashflowType.INCOME }

            // 3️⃣ Map & merge expenses → TransactionEntity (Room upsert)
            if (expenses.isNotEmpty()) {
                val transactionEntities = expenses.map { entry ->
                    TransactionEntity(
                        id = entry.id,
                        name = entry.title,
                        category = entry.category,
                        amount = entry.amount,
                        datetime = entry.date,
                        note = null,
                        isSynced = true,
                        remoteId = entry.id,
                        createdAt = entry.date,
                        updatedAt = entry.date,
                        groupId = entry.groupId,
                        groupName = entry.groupName
                    )
                }
                transactionDao.insertTransactions(transactionEntities)
            }

            // 4️⃣ Map & merge incomes → IncomeEntity (Room upsert)
            if (incomes.isNotEmpty()) {
                val incomeEntities = incomes.map { entry ->
                    IncomeEntity(
                        id = entry.id, // Using server ID as primary key
                        name = entry.title,
                        amount = entry.amount,
                        datetime = entry.date,
                        type = try { entry.category.let { IncomeType.valueOf(it.uppercase()) } } catch (e: Exception) { IncomeType.OTHER },
                        source = null,
                        assetId = null,
                        isRecurring = false,
                        frequency = null,
                        note = null,
                        isSynced = true,
                        remoteId = entry.id,
                        createdAt = entry.date,
                        updatedAt = entry.date
                    )
                }
                incomeDao.insertIncomes(incomeEntities)
            }

            return result.entries
        } catch (e: Exception) {
            // On failure, fall back to local data
            return loadFromLocal(startDate = Date(0), endDate = Date())
        }
    }

    /**
     * Sync and fetch ALL pages of cashflow data
     * @param month Optional month filter (format: "2026-06")
     * @param year Optional year filter (format: "2026")
     * @return Complete list of all entries
     */
    suspend fun syncAndFetchAll(
        month: String? = null,
        year: String? = null
    ): List<CashflowEntry> {
        android.util.Log.d(TAG, "🔄 Starting syncAndFetchAll - month: ${month ?: "all"}, year: ${year ?: "all"}")
        
        try {
            // 1️⃣ Fetch ALL pages from remote
            val allEntries = cashflowRepository.getHistoryAllPages(
                month = month,
                year = year,
                pageSize = 100
            )
            
            if (allEntries.isEmpty()) {
                android.util.Log.d(TAG, "ℹ️ No entries returned from API")
                return emptyList()
            }
            
            // 2️⃣ Split by type
            val expenses = allEntries.filter { it.type == CashflowType.EXPENSE }
            val incomes = allEntries.filter { it.type == CashflowType.INCOME }
            
            android.util.Log.d(TAG, "💾 Merging to local DB: ${expenses.size} expenses, ${incomes.size} incomes")
            
            // 3️⃣ Map & merge expenses → TransactionEntity
            if (expenses.isNotEmpty()) {
                val transactionEntities = expenses.map { it.toTransactionEntity() }
                transactionDao.insertTransactions(transactionEntities)
            }
            
            // 4️⃣ Map & merge incomes → IncomeEntity
            if (incomes.isNotEmpty()) {
                val incomeEntities = incomes.map { it.toIncomeEntity() }
                incomeDao.insertIncomes(incomeEntities)
            }
            
            android.util.Log.d(TAG, "✅ syncAndFetchAll complete: ${allEntries.size} items")
            return allEntries
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ syncAndFetchAll failed", e)
            throw e
        }
    }
    
    // Extension functions for mapping
    private fun CashflowEntry.toTransactionEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            name = title,
            category = category,
            amount = amount,
            datetime = date,
            note = null,
            isSynced = true,
            remoteId = id,
            createdAt = date,
            updatedAt = date,
            groupId = groupId,
            groupName = groupName
        )
    }
    
    private fun CashflowEntry.toIncomeEntity(): IncomeEntity {
        return IncomeEntity(
            id = id,
            name = title,
            amount = amount,
            datetime = date,
            type = try { 
                category.let { IncomeType.valueOf(it.uppercase()) } 
            } catch (e: Exception) { 
                IncomeType.OTHER 
            },
            source = null,
            assetId = null,
            isRecurring = false,
            frequency = null,
            note = null,
            isSynced = true,
            remoteId = id,
            createdAt = date,
            updatedAt = date
        )
    }

    /**
     * Combines TransactionEntity + IncomeEntity from local Room DB into unified CashflowEntry list.
     * This is the reverse path for offline display.
     */
    suspend fun loadFromLocal(startDate: Date, endDate: Date): List<CashflowEntry> {
        // Expenses from TransactionDao
        val transactions = transactionDao.getTransactionsSince(startDate.time)
            .filter { it.datetime.time <= endDate.time }
            .map { entity ->
                CashflowEntry(
                    id = entity.id,
                    title = entity.name,
                    amount = entity.amount,
                    category = entity.category,
                    type = CashflowType.EXPENSE,
                    date = entity.datetime,
                    groupId = entity.groupId,
                    groupName = entity.groupName
                )
            }

        // Incomes from IncomeDao
        val allIncomes = incomeDao.getAllIncomesOnce()
        val incomes = allIncomes
            .filter { it.datetime.time >= startDate.time && it.datetime.time <= endDate.time }
            .map { entity ->
                CashflowEntry(
                    id = entity.id,
                    title = entity.name,
                    amount = entity.amount,
                    category = entity.type.name,
                    type = CashflowType.INCOME,
                    date = entity.datetime
                )
            }

        // Combine + sort by datetime descending
        return (transactions + incomes).sortedByDescending { it.date }
    }

    /**
     * Calculate a summary from local data for a given period.
     */
    suspend fun calculateSummaryFromLocal(
        startDate: Date,
        endDate: Date,
        monthLabel: String
    ): CashflowSummary {
        val entries = loadFromLocal(startDate, endDate)

        val totalIncome = entries.filter { it.type == CashflowType.INCOME }.sumOf { it.amount }
        val totalExpense = entries.filter { it.type == CashflowType.EXPENSE }.sumOf { it.amount }

        return CashflowSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = totalIncome - totalExpense,
            periodLabel = monthLabel
        )
    }

    companion object {
        private const val TAG = "CashflowSyncUseCase"
    }
}

class TransactionSyncUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Pushes unsynced local transactions to the remote server.
     */
    suspend fun syncLocalTransactionsToRemote() {
        transactionRepository.syncTransactions()
    }
}
