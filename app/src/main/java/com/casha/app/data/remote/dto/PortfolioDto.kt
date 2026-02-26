package com.casha.app.data.remote.dto

import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetType
import com.casha.app.domain.model.AssetTransaction
import com.casha.app.domain.model.AssetTransactionType
import com.casha.app.domain.model.AssetBreakdown
import com.casha.app.domain.model.PortfolioSummary
import com.casha.app.domain.model.IncomeType
import com.casha.app.domain.model.IncomeFrequency
import com.casha.app.core.util.CurrencyFormatter
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class AssetDto(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val description: String? = null,
    val userId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val incomeEntries: List<IncomeDto>? = null,
    
    // Quantity-based fields
    val quantity: Double? = null,
    val unit: String? = null,
    val pricePerUnit: Double? = null,
    
    // Additional fields
    val acquisitionDate: String? = null,
    val location: String? = null
) {
    fun toDomain(): Asset {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        fun parseDate(dateStr: String?): Date {
            if (dateStr == null) return Date()
            return try {
                isoFormat.parse(dateStr) ?: Date()
            } catch (e: Exception) {
                try {
                    simpleDateFormat.parse(dateStr) ?: Date()
                } catch (e2: Exception) {
                    Date()
                }
            }
        }

        return Asset(
            id = id ?: "",
            name = name ?: "",
            type = type?.let { 
                try { AssetType.valueOf(it.uppercase()) } catch (e: Exception) { AssetType.OTHER } 
            } ?: AssetType.OTHER,
            amount = amount ?: 0.0,
            currency = currency ?: CurrencyFormatter.defaultCurrency,
            description = description,
            userId = userId ?: "",
            createdAt = parseDate(createdAt),
            updatedAt = parseDate(updatedAt),
            incomeEntries = incomeEntries?.map { dto ->
                com.casha.app.domain.model.IncomeCasha(
                    id = dto.id,
                    name = dto.name,
                    amount = dto.amount,
                    datetime = parseDate(dto.datetime),
                    type = try { IncomeType.valueOf(dto.type.uppercase()) } catch(e: Exception) { IncomeType.OTHER },
                    source = dto.source,
                    assetId = dto.assetId,
                    isRecurring = dto.isRecurring,
                    frequency = dto.frequency?.let { 
                        try { IncomeFrequency.valueOf(it.uppercase()) } catch(e: Exception) { null } 
                    },
                    note = dto.note,
                    createdAt = dto.createdAt?.let { parseDate(it) } ?: Date(),
                    updatedAt = dto.updatedAt?.let { parseDate(it) } ?: Date()
                )
            },
            quantity = quantity,
            unit = unit,
            pricePerUnit = pricePerUnit,
            acquisitionDate = acquisitionDate?.let { 
                try { isoFormat.parse(it) } catch (e: Exception) { 
                    try { simpleDateFormat.parse(it) } catch (e2: Exception) { null }
                }
            },
            location = location
        )
    }
}

@Serializable
data class AssetTransactionDto(
    val id: String? = null,
    val assetId: String? = null,
    val type: String? = null,
    val quantity: Double? = null,
    val pricePerUnit: Double? = null,
    val totalAmount: Double? = null,
    val datetime: String? = null,
    val note: String? = null,
    val costBasis: Double? = null,
    val profitLoss: Double? = null,
    val createdAt: String? = null
) {
    fun toDomain(): AssetTransaction {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        fun parseDate(dateStr: String?): Date {
            if (dateStr == null) return Date()
            return try {
                isoFormat.parse(dateStr) ?: Date()
            } catch (e: Exception) {
                try {
                    simpleDateFormat.parse(dateStr) ?: Date()
                } catch (e2: Exception) {
                    Date()
                }
            }
        }

        return AssetTransaction(
            id = id ?: "",
            assetId = assetId ?: "",
            type = type?.let {
                try { AssetTransactionType.valueOf(it.uppercase()) } catch(e: Exception) { AssetTransactionType.SAVING }
            } ?: AssetTransactionType.SAVING,
            quantity = quantity,
            pricePerUnit = pricePerUnit,
            totalAmount = totalAmount ?: 0.0,
            datetime = parseDate(datetime),
            note = note,
            costBasis = costBasis,
            profitLoss = profitLoss,
            createdAt = parseDate(createdAt)
        )
    }
}

@Serializable
data class AssetBreakdownDto(
    val type: String? = null,
    val amount: Double? = null,
    val count: Int? = null
) {
    fun toDomain(): AssetBreakdown {
        return AssetBreakdown(
            type = type?.let { 
                try { AssetType.valueOf(it.uppercase()) } catch (e: Exception) { AssetType.OTHER } 
            } ?: AssetType.OTHER,
            amount = amount ?: 0.0,
            count = count ?: 0
        )
    }
}

@Serializable
data class PortfolioSummaryDto(
    val currency: String? = null,
    val totalAssets: Double? = null,
    val breakdown: List<AssetBreakdownDto>? = null,
    val assets: List<AssetDto>? = null,
    val lastUpdated: String? = null
) {
    fun toDomain(): PortfolioSummary {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        
        fun parseDate(dateStr: String?): Date = try {
            dateStr?.let { isoFormat.parse(it) } ?: Date()
        } catch (e: Exception) {
            Date()
        }

        return PortfolioSummary(
            currency = currency ?: CurrencyFormatter.defaultCurrency,
            totalAssets = totalAssets ?: 0.0,
            breakdown = breakdown?.map { it.toDomain() } ?: emptyList(),
            assets = assets?.map { it.toDomain() } ?: emptyList(),
            lastUpdated = parseDate(lastUpdated)
        )
    }
}

@Serializable
data class CreateAssetRequestDto(
    val name: String,
    val type: String,
    val amount: Double? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val pricePerUnit: Double? = null,
    val currency: String? = null,
    val description: String? = null,
    val location: String? = null,
    val acquisitionDate: String? = null
)

@Serializable
data class UpdateAssetRequestDto(
    val name: String? = null,
    val amount: Double? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val pricePerUnit: Double? = null,
    val location: String? = null,
    val acquisitionDate: String? = null
)

@Serializable
data class CreateAssetTransactionRequestDto(
    val type: String,
    val quantity: Double? = null,
    val pricePerUnit: Double? = null,
    val amount: Double? = null,
    val note: String? = null,
    val datetime: String? = null
)
