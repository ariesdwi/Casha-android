package com.casha.app.data.remote.api

import com.casha.app.data.remote.dto.*
import retrofit2.http.*

/**
 * Retrofit service for transaction-related endpoints.
 */
interface TransactionApiService {

    @GET("transactions")
    suspend fun getTransactions(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null
    ): TransactionListResponse

    @POST("transactions")
    suspend fun createTransaction(
        @Body request: TransactionUploadDto
    ): TransactionDto

    @PATCH("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body request: UpdateTransactionDto
    ): TransactionDto

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String
    ): BaseResponse<Unit>
}
