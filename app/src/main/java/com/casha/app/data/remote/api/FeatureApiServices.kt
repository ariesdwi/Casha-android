package com.casha.app.data.remote.api

import com.casha.app.data.remote.dto.*
import retrofit2.http.*

interface CashflowApiService {
    @GET("cashflow/history")
    suspend fun getHistory(
        @Query("month") month: String? = null,
        @Query("year") year: String? = null,
        @Query("page") page: Int? = 1,
        @Query("pageSize") pageSize: Int? = 50
    ): BaseResponse<CashflowHistoryResponseDto>

    @GET("cashflow/summary")
    suspend fun getSummary(
        @Query("month") month: String? = null,
        @Query("year") year: String? = null
    ): BaseResponse<CashflowSummaryDto>
}

interface IncomeApiService {
    @GET("income")
    suspend fun getIncomes(): BaseResponse<List<IncomeDto>>

    @GET("income/summary")
    suspend fun getSummary(
        @Query("period") period: String? = null
    ): BaseResponse<IncomeSummaryDto>
}

interface GoalApiService {
    @GET("goals")
    suspend fun getGoals(): BaseResponse<List<GoalDto>>

    @GET("goals/summary")
    suspend fun getSummary(): BaseResponse<GoalSummaryDto>

    @GET("goals/categories")
    suspend fun getGoalsCategories(): BaseResponse<List<GoalCategoryDto>>
}

interface CategoryApiService {
    @GET("categories")
    suspend fun getAllCategories(): BaseResponse<List<CategoryDto>>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): BaseResponse<CategoryDto>

    @PATCH("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: UpdateCategoryRequest
    ): BaseResponse<CategoryDto>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): BaseResponse<Unit>
}
