package com.casha.app.data.remote.impl

import com.casha.app.core.network.NetworkError
import com.casha.app.core.network.safeApiCall
import com.casha.app.data.remote.api.PortfolioApiService
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
        val body = mutableMapOf<String, Any>(
            "name" to request.name,
            "type" to request.type.rawValue
        )

        request.amount?.let { body["amount"] = it }
        request.quantity?.let { body["quantity"] = it }
        request.unit?.let { body["unit"] = it }
        request.pricePerUnit?.let { body["pricePerUnit"] = it }
        request.currency?.let { body["currency"] = it }
        request.description?.let { body["description"] = it }
        request.location?.let { body["location"] = it }
        
        request.acquisitionDate?.let { 
            body["acquisitionDate"] = simpleDateFormatter.format(it)
        }

        val result = safeApiCall { api.createAsset(body) }
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
        val body = mutableMapOf<String, Any>()
        
        request.name?.let { body["name"] = it }
        request.amount?.let { body["amount"] = it }
        request.quantity?.let { body["quantity"] = it }
        request.unit?.let { body["unit"] = it }
        request.pricePerUnit?.let { body["pricePerUnit"] = it }
        request.location?.let { body["location"] = it }
        
        request.acquisitionDate?.let {
            body["acquisitionDate"] = simpleDateFormatter.format(it)
        }

        val result = safeApiCall { api.updateAsset(id, body) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun deleteAsset(id: String): Asset {
        val result = safeApiCall { api.deleteAsset(id) }
        return result.fold(
            onSuccess = { response -> response.data?.toDomain() ?: throw NetworkError.RequestFailed("Invalid response") },
            onFailure = { throw it }
        )
    }

    override suspend fun addAssetTransaction(assetId: String, request: CreateAssetTransactionRequest): AssetTransaction {
        val body = mutableMapOf<String, Any>(
            "type" to request.type.rawValue
        )

        request.quantity?.let { body["quantity"] = it }
        request.pricePerUnit?.let { body["pricePerUnit"] = it }
        request.amount?.let { body["amount"] = it }
        request.note?.let { body["note"] = it }
        
        request.datetime?.let {
            body["datetime"] = isoFormatter.format(it)
        }

        val result = safeApiCall { api.addAssetTransaction(assetId, body) }
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
