package com.casha.app.data.remote.impl

import com.casha.app.core.network.NetworkError
import com.casha.app.core.network.safeApiCall
import com.casha.app.data.remote.api.PortfolioApiService
import com.casha.app.data.remote.dto.*
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.PortfolioRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    private val api: PortfolioApiService
) : PortfolioRepository {

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
    private val simpleDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override suspend fun createAsset(request: CreateAssetRequest): Asset {
        val dto = CreateAssetRequestDto(
            name = request.name,
            type = request.type.rawValue,
            amount = request.amount,
            quantity = request.quantity,
            unit = request.unit,
            pricePerUnit = request.pricePerUnit,
            currency = request.currency,
            description = request.description,
            location = request.location,
            acquisitionDate = request.acquisitionDate?.let { simpleDateFormatter.format(it) }
        )

        val result = safeApiCall { api.createAsset(dto) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun getAssets(): List<Asset> {
        val result = safeApiCall { api.getAssets() }
        return result.fold(
            onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() },
            onFailure = { emptyList() }
        )
    }

    override suspend fun getPortfolioSummary(): PortfolioSummary {
        val result = safeApiCall { api.getPortfolioSummary() }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun updateAsset(id: String, request: UpdateAssetRequest): Asset {
        val dto = UpdateAssetRequestDto(
            name = request.name,
            amount = request.amount,
            quantity = request.quantity,
            unit = request.unit,
            pricePerUnit = request.pricePerUnit,
            location = request.location,
            acquisitionDate = request.acquisitionDate?.let { simpleDateFormatter.format(it) }
        )

        val result = safeApiCall { api.updateAsset(id, dto) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun deleteAsset(id: String) {
        val result = safeApiCall { api.deleteAsset(id) }
        result.fold(
            onSuccess = { /* Handle success */ },
            onFailure = { throw it }
        )
    }

    override suspend fun addAssetTransaction(assetId: String, request: CreateAssetTransactionRequest): AssetTransaction {
        val dto = CreateAssetTransactionRequestDto(
            type = request.type.rawValue,
            quantity = request.quantity,
            pricePerUnit = request.pricePerUnit,
            amount = request.amount,
            note = request.note,
            datetime = request.datetime?.let { isoFormatter.format(it) }
        )

        val result = safeApiCall { api.addAssetTransaction(assetId, dto) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun getAssetTransactions(assetId: String): List<AssetTransaction> {
        val result = safeApiCall { api.getAssetTransactions(assetId) }
        return result.fold(
            onSuccess = { response -> response.data?.map { it.toDomain() } ?: emptyList() },
            onFailure = { emptyList() }
        )
    }
}
