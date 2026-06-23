package com.casha.app.data.remote.impl

import android.util.Log
import com.casha.app.data.local.entity.IncomeEntity
import com.casha.app.data.remote.api.IncomeApiService
import com.casha.app.data.remote.api.CashflowApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.IncomeRepository
import com.casha.app.core.network.SyncEventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import com.casha.app.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepositoryImpl @Inject constructor(
    private val apiService: IncomeApiService,
    private val cashflowApiService: CashflowApiService,
    private val incomeDao: com.casha.app.data.local.dao.IncomeDao,
    private val syncEventBus: SyncEventBus
) : IncomeRepository {
    
    companion object {
        private const val TAG = "IncomeRepository"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun getIncomesFlow(): Flow<List<IncomeCasha>> {
        return incomeDao.getAllIncomes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getIncomes(): List<IncomeCasha> {
        Log.d(TAG, "📥 Fetching incomes from API")
        val result = safeApiCall { apiService.getIncomes() }
        return result.fold(
            onSuccess = { response -> 
                Log.d(TAG, "✓ Fetched ${response.data?.size ?: 0} incomes")
                response.data?.map { it.toDomain() } ?: emptyList()
            },
            onFailure = { exception ->
                Log.w(TAG, "⚠️ Failed to fetch from API, using local data: ${exception.message}")
                // Fallback to local
                incomeDao.getAllIncomesOnce().map { it.toDomain() }
            }
        )
    }

    override suspend fun getSummary(period: String?): IncomeSummary {
        Log.d(TAG, "📊 Fetching income summary for period: $period")
        val result = safeApiCall { apiService.getSummary(period) }
        return result.fold(
            onSuccess = { response -> 
                Log.d(TAG, "✓ Income summary: total=${response.data?.totalIncome}")
                response.data?.toDomain() ?: IncomeSummary(0.0, 0, emptyList())
            },
            onFailure = { exception ->
                Log.w(TAG, "⚠️ Failed to fetch summary: ${exception.message}")
                IncomeSummary(0.0, 0, emptyList())
            }
        )
    }

    override suspend fun saveIncome(request: CreateIncomeRequest) {
        Log.d(TAG, "💰 saveIncome() called: name=${request.name}, amount=${request.amount}, assetId=${request.assetId}")
        
        val dto = CreateIncomeRequestDto(
            name = request.name,
            type = request.type.name,
            amount = request.amount,
            datetime = dateFormat.format(request.datetime),
            source = request.source,
            isRecurring = request.isRecurring,
            frequency = request.frequency?.name,
            note = request.note,
            assetId = request.assetId
        )

        // 1️⃣ OPTIMISTIC SAVE: Save locally immediately for UI responsiveness
        val localId = UUID.randomUUID().toString()
        val localEntity = IncomeEntity(
            id = localId,
            name = request.name,
            amount = request.amount,
            datetime = request.datetime,
            type = request.type,
            source = request.source,
            assetId = request.assetId,
            isRecurring = request.isRecurring,
            frequency = request.frequency,
            note = request.note,
            isSynced = false,  // Mark as pending sync
            remoteId = null,
            createdAt = Date(),
            updatedAt = Date()
        )
        incomeDao.insertIncome(localEntity)
        Log.d(TAG, "✓ Income saved locally with ID: $localId")

        // 2️⃣ ASYNC REMOTE SYNC: Send to backend
        try {
            val result = safeApiCall { apiService.createIncome(dto) }
            
            result.onSuccess { response ->
                Log.d(TAG, "✅ Income created on backend: id=${response.data?.id}")
                
                // Update local entity with server data
                val syncedEntity = IncomeEntity(
                    id = response.data?.id ?: localId,
                    name = request.name,
                    amount = request.amount,
                    datetime = request.datetime,
                    type = request.type,
                    source = request.source,
                    assetId = request.assetId,
                    isRecurring = request.isRecurring,
                    frequency = request.frequency,
                    note = request.note,
                    isSynced = true,  // Mark as synced
                    remoteId = response.data?.id,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                incomeDao.insertIncome(syncedEntity)
                
                // 3️⃣ TRIGGER WALLET REFRESH: If income is linked to wallet
                if (request.assetId != null) {
                    Log.d(TAG, "🔄 Income linked to wallet (${request.assetId}), triggering sync event")
                    syncEventBus.emitSyncCompleted()
                }
            }
            
            result.onFailure { exception ->
                Log.e(TAG, "❌ Failed to sync income to backend: ${exception.message}", exception)
                // Keep local copy marked as pending sync for retry later
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during income sync: ${e.message}", e)
            // Keep local copy marked as pending sync for retry later
        }
    }

    override suspend fun updateIncome(id: String, request: CreateIncomeRequest) {
        Log.d(TAG, "✏️ updateIncome() called: id=$id, name=${request.name}")
        
        // 1. Remote-First Failsafe: Try syncing to remote
        val dto = UpdateTransactionDto(
            name = request.name,
            category = request.type.name,
            amount = request.amount,
            datetime = dateFormat.format(request.datetime)
        )
        val result = safeApiCall { cashflowApiService.updateCashflow("INCOME", id, dto) }
        
        // We NEED the API to succeed here for the local update to be valid
        result.onFailure { 
            Log.e(TAG, "❌ Failed to update income on backend: ${it.message}")
            throw it 
        }
        
        // 2. Only if remote succeeds, save locally
        val entity = IncomeEntity(
            id = id,
            name = request.name,
            amount = request.amount,
            datetime = request.datetime,
            type = request.type,
            source = request.source,
            assetId = request.assetId,
            isRecurring = request.isRecurring,
            frequency = request.frequency,
            note = request.note,
            isSynced = true,
            remoteId = id,
            updatedAt = Date()
        )
        incomeDao.insertIncome(entity)
        Log.d(TAG, "✓ Income updated successfully")
        
        // Trigger wallet refresh if linked to asset
        if (request.assetId != null) {
            Log.d(TAG, "🔄 Income linked to wallet, triggering sync event")
            syncEventBus.emitSyncCompleted()
        }
    }

    override suspend fun deleteIncome(id: String) {
        Log.d(TAG, "🗑️ deleteIncome() called: id=$id")
        
        // 1. Remote-First Failsafe: Try deleting from API
        val result = safeApiCall { cashflowApiService.deleteCashflow("INCOME", id) }
        result.onFailure { 
            Log.e(TAG, "❌ Failed to delete income from backend: ${it.message}")
            throw it 
        }
        
        // 2. Only if remote succeeds, delete locally
        incomeDao.deleteById(id)
        Log.d(TAG, "✓ Income deleted successfully")
        
        // Trigger dashboard refresh after deletion
        Log.d(TAG, "🔄 Triggering sync event after income deletion")
        syncEventBus.emitSyncCompleted()
    }

    private fun IncomeDto.toDomain() = IncomeCasha(
        id = id,
        name = name,
        amount = amount,
        datetime = try { dateFormat.parse(datetime) ?: Date() } catch (e: Exception) { Date() },
        type = try { IncomeType.valueOf(type.uppercase()) } catch (e: Exception) { IncomeType.OTHER },
        source = source,
        assetId = assetId,
        isRecurring = isRecurring,
        frequency = try { frequency?.let { IncomeFrequency.valueOf(it.uppercase()) } } catch (e: Exception) { null },
        note = note,
        isSynced = true,
        remoteId = id,
        createdAt = try { createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
        updatedAt = try { updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() }
    )

    private fun IncomeSummaryDto.toDomain() = IncomeSummary(
        totalIncome = totalIncome,
        count = count,
        byType = emptyList() // Backend currently doesn't provide breakdown by type in the summary
    )

    private fun IncomeEntity.toDomain() = IncomeCasha(
        id = id,
        name = name,
        amount = amount,
        datetime = datetime,
        type = type,
        source = source,
        assetId = assetId,
        isRecurring = isRecurring,
        frequency = frequency,
        note = note,
        isSynced = isSynced,
        remoteId = remoteId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
