package com.casha.app.data.remote.api

import com.casha.app.core.config.ApiEndpoints
import com.casha.app.data.remote.dto.*
import retrofit2.http.*

interface WalletApiService {

    @GET(ApiEndpoints.WALLETS)
    suspend fun getWallets(): BaseResponse<List<WalletDto>>

    @GET(ApiEndpoints.WALLETS_SUMMARY)
    suspend fun getWalletSummary(): BaseResponse<WalletSummaryDto>

    @PATCH(ApiEndpoints.WALLETS_DEFAULT)
    suspend fun setDefaultWallet(
        @Body request: SetDefaultWalletRequestDto
    ): BaseResponse<Unit?>

    @DELETE(ApiEndpoints.WALLETS_DEFAULT)
    suspend fun clearDefaultWallet(): BaseResponse<Unit?>

    @POST(ApiEndpoints.WALLETS_TRANSFER)
    suspend fun transferWallet(
        @Body request: TransferWalletRequestDto
    ): BaseResponse<TransferWalletResponseDto>

    @POST(ApiEndpoints.ASSET_CREATE)
    suspend fun createLiquidWallet(
        @Body request: AddLiquidWalletRequestDto
    ): BaseResponse<WalletDto>

    @POST(ApiEndpoints.LIABILITY_CREATE)
    suspend fun createCreditCard(
        @Body request: AddCreditCardRequestDto
    ): BaseResponse<WalletDto>

    @PATCH("assets/{id}")
    suspend fun updateAssetWallet(
        @Path("id") id: String,
        @Body request: UpdateWalletRequestDto
    ): BaseResponse<WalletDto>

    @PATCH("loans/{id}")
    suspend fun updateLoanWallet(
        @Path("id") id: String,
        @Body request: UpdateWalletRequestDto
    ): BaseResponse<WalletDto>

    @DELETE("assets/{id}")
    suspend fun deleteAssetWallet(
        @Path("id") id: String
    ): BaseResponse<Unit?>

    @DELETE("loans/{id}")
    suspend fun deleteLoanWallet(
        @Path("id") id: String
    ): BaseResponse<Unit?>
}
