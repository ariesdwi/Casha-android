package com.casha.app.data.remote.impl

import com.casha.app.data.local.entity.IncomeEntity
import com.casha.app.data.remote.api.IncomeApiService
import com.casha.app.data.remote.api.CashflowApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.IncomeRepository
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
    private val incomeDao: com.casha.app.data.local.dao.IncomeDao
) : IncomeRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun getIncomesFlow(): Flow<List<IncomeCasha>> {
        return incomeDao.getAllIncomes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getIncomes(): List<IncomeCasha> {
        val result = safeApiCall { apiService.getIncomes() }
        return result.fold(
            onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() },
            onFailure = { 
                // Fallback to local
                incomeDao.getAllIncomesOnce().map { it.toDomain() }
            }
        )
    }

    override suspend fun getSummary(period: String?): IncomeSummary {
        val result = safeApiCall { apiService.getSummary(period) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: IncomeSummary(0.0, 0, emptyList()) },
            onFailure = { IncomeSummary(0.0, 0, emptyList()) }
        )
    }

    override suspend fun saveIncome(request: CreateIncomeRequest) {
        val entity = IncomeEntity(
            id = UUID.randomUUID().toString(),
            name = request.name,
            amount = request.amount,
            datetime = request.datetime,
            type = request.type,
            source = request.source,
            assetId = request.assetId,
            isRecurring = request.isRecurring,
            frequency = request.frequency,
            note = request.note,
            createdAt = Date(),
            updatedAt = Date()
        )

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

        safeApiCall { apiService.createIncome(dto) }.fold(
            onSuccess = { incomeDao.insertIncome(entity) },
            onFailure = { incomeDao.insertIncome(entity) } // Still fallback
        )
    }

    override suspend fun updateIncome(id: String, request: CreateIncomeRequest) {
        // 1. Remote-First Failsafe: Try syncing to remote
        val dto = UpdateTransactionDto(
            name = request.name,
            category = request.type.name,
            amount = request.amount,
            datetime = dateFormat.format(request.datetime)
        )
        val result = safeApiCall { cashflowApiService.updateCashflow("INCOME", id, dto) }
        // We NEED the API to succeed here for the local update to be valid
        result.onFailure { throw it }
        
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
            updatedAt = Date()
        )
        incomeDao.insertIncome(entity)
    }

    override suspend fun deleteIncome(id: String) {
        // 1. Remote-First Failsafe: Try deleting from API
        val result = safeApiCall { cashflowApiService.deleteCashflow("INCOME", id) }
        result.onFailure { throw it }
        
        // 2. Only if remote succeeds, delete locally
        incomeDao.deleteById(id)
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
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
