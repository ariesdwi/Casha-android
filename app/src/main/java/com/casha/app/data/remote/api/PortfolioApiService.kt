package com.casha.app.data.remote.api

import com.casha.app.core.config.ApiEndpoints
import com.casha.app.data.remote.dto.*
import retrofit2.http.*

interface PortfolioApiService {

    @POST(ApiEndpoints.ASSET_CREATE)
    suspend fun createAsset(
        @Body request: CreateAssetRequestDto
    ): BaseResponse<AssetDto>

    @GET(ApiEndpoints.ASSET_LIST)
    suspend fun getAssets(): BaseResponse<List<AssetDto>>

    @GET(ApiEndpoints.PORTFOLIO_SUMMARY)
    suspend fun getPortfolioSummary(): BaseResponse<PortfolioSummaryDto>

    @PATCH("assets/{id}")
    suspend fun updateAsset(
        @Path("id") id: String,
        @Body request: UpdateAssetRequestDto
    ): BaseResponse<AssetDto>

    @DELETE("assets/{id}")
    suspend fun deleteAsset(
        @Path("id") id: String
    ): BaseResponse<Unit?>

    @POST("assets/{assetId}/transactions")
    suspend fun addAssetTransaction(
        @Path("assetId") assetId: String,
        @Body request: CreateAssetTransactionRequestDto
    ): BaseResponse<AssetTransactionDto>

    @GET("assets/{assetId}/transactions")
    suspend fun getAssetTransactions(
        @Path("assetId") assetId: String
    ): BaseResponse<List<AssetTransactionDto>>
}
