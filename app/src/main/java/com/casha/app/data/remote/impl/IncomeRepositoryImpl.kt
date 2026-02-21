package com.casha.app.data.remote.impl

import com.casha.app.data.remote.api.IncomeApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.IncomeRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepositoryImpl @Inject constructor(
    private val apiService: IncomeApiService
) : IncomeRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override suspend fun getIncomes(): List<IncomeCasha> {
        val response = apiService.getIncomes()
        return response.data?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getSummary(period: String?): IncomeSummary {
        val response = apiService.getSummary(period)
        return response.data?.toDomain() ?: IncomeSummary(0.0, 0, emptyList())
    }

    private fun IncomeDto.toDomain() = IncomeCasha(
        id = id,
        name = source,
        amount = amount,
        datetime = try { dateFormat.parse(datetime) ?: Date() } catch (e: Exception) { Date() },
        type = try { IncomeType.valueOf(category.uppercase()) } catch (e: Exception) { IncomeType.OTHER },
        source = source,
        assetId = null,
        isRecurring = false,
        frequency = null,
        note = note,
        createdAt = try { createdAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() },
        updatedAt = try { updatedAt?.let { dateFormat.parse(it) } ?: Date() } catch (e: Exception) { Date() }
    )

    private fun IncomeSummaryDto.toDomain() = IncomeSummary(
        totalIncome = totalIncome,
        count = count,
        byType = emptyList() // Backend currently doesn't provide breakdown by type in the summary
    )
}
