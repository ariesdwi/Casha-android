package com.casha.app.data.remote.api

import com.casha.app.core.config.ApiEndpoints
import com.casha.app.data.remote.dto.*
import retrofit2.http.*

interface LiabilityApiService {

    @POST(ApiEndpoints.LIABILITY_CREATE)
    suspend fun createLiability(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<LiabilityDto>

    @GET(ApiEndpoints.LIABILITY_LIST)
    suspend fun getLiabilities(
        @Query("status") status: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null
    ): BaseResponse<LiabilityListDTO>

    @GET(ApiEndpoints.LIABILITY_SUMMARY)
    suspend fun getLiabilitySummary(): BaseResponse<LiabilitySummaryDTO>

    @POST("liabilities/{id}/payments") // Using explicit string paths mapped from ApiEndpoints functions
    suspend fun recordPayment(
        @Path("id") liabilityId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<LiabilityPaymentDTO>

    @GET("liabilities/{id}")
    suspend fun getLiabilityDetails(
        @Path("id") id: String
    ): BaseResponse<LiabilityDto>

    @POST("liabilities/{id}/transactions")
    suspend fun createTransaction(
        @Path("id") liabilityId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<LiabilityTransactionDTO>

    @GET("liabilities/{id}/statements/latest")
    suspend fun getLatestStatement(
        @Path("id") liabilityId: String
    ): BaseResponse<LiabilityStatementDTO>

    @GET("liabilities/{id}/statements")
    suspend fun getAllStatements(
        @Path("id") liabilityId: String
    ): BaseResponse<LiabilityStatementsResponseDTO>

    @GET("liabilities/{id}/statements/{statementId}")
    suspend fun getStatementDetails(
        @Path("id") liabilityId: String,
        @Path("statementId") statementId: String
    ): BaseResponse<StatementDetailDataDTO>

    @GET("liabilities/{id}/unbilled")
    suspend fun getUnbilledTransactions(
        @Path("id") liabilityId: String
    ): BaseResponse<UnbilledTransactionsDTO>

    @GET("liabilities/{id}/insights")
    suspend fun getLiabilityInsights(
        @Path("id") liabilityId: String
    ): BaseResponse<LiabilityInsightDTO>

    @GET("liabilities/{id}/payments")
    suspend fun getPaymentHistory(
        @Path("id") liabilityId: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): BaseResponse<LiabilityPaymentHistoryDTO>

    @GET("liabilities/{id}/transactions")
    suspend fun getTransactions(
        @Path("id") liabilityId: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): BaseResponse<List<LiabilityTransactionDTO>>

    @DELETE("liabilities/{id}")
    suspend fun deleteLiability(
        @Path("id") id: String
    ): BaseResponse<Unit>

    @POST("liabilities/{id}/transactions/{transactionId}/convert")
    suspend fun convertToInstallment(
        @Path("id") liabilityId: String,
        @Path("transactionId") transactionId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<InstallmentPlanDTO>

    @POST("liabilities/{id}/installments")
    suspend fun addInstallment(
        @Path("id") liabilityId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<InstallmentPlanDTO>

    @POST("liabilities/simulate")
    suspend fun simulatePayoff(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse<SimulatePayoffResponseDTO>
}
