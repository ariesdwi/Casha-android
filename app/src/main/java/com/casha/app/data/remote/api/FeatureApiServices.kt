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
    
    @PATCH("cashflow/{type}/{id}")
    suspend fun updateCashflow(
        @Path("type") type: String,
        @Path("id") id: String,
        @Body request: UpdateTransactionDto
    ): BaseResponse<CashflowDto>
    
    @POST("transactions/create")
    suspend fun createTransaction(
        @Body request: TransactionUploadDto
    ): BaseResponse<CashflowDto>
    
    @DELETE("cashflow/{type}/{id}")
    suspend fun deleteCashflow(
        @Path("type") type: String,
        @Path("id") id: String
    ): BaseResponse<Unit>
}

interface IncomeApiService {
    @GET("income")
    suspend fun getIncomes(): BaseResponse<List<IncomeDto>>

    @GET("income/summary")
    suspend fun getSummary(
        @Query("period") period: String? = null
    ): BaseResponse<IncomeSummaryDto>

    @POST("income")
    suspend fun createIncome(@Body request: CreateIncomeRequestDto): BaseResponse<IncomeDto>
}

interface GoalApiService {
    @GET("goals")
    suspend fun getGoals(): BaseResponse<List<GoalDto>>

    @GET("goals/summary")
    suspend fun getSummary(): BaseResponse<GoalSummaryDto>

    @GET("goals/categories")
    suspend fun getGoalsCategories(): BaseResponse<List<GoalCategoryDto>>

    @POST("goals")
    suspend fun createGoal(@Body request: CreateGoalApiRequest): BaseResponse<GoalDto>

    @GET("goals/{id}")
    suspend fun getGoal(@Path("id") id: String): BaseResponse<GoalDto>

    @PATCH("goals/{id}")
    suspend fun updateGoal(
        @Path("id") id: String,
        @Body request: CreateGoalApiRequest
    ): BaseResponse<GoalDto>

    @DELETE("goals/{id}")
    suspend fun deleteGoal(@Path("id") id: String): BaseResponse<Unit>

    @POST("goals/{id}/contributions")
    suspend fun addContribution(
        @Path("id") id: String,
        @Body request: AddContributionApiRequest
    ): BaseResponse<GoalContributionDto>
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

interface BudgetApiService {
    @GET("budgets")
    suspend fun getBudgets(
        @Query("month") month: String? = null
    ): BaseResponse<List<BudgetDto>>

    @GET("budgets/summary")
    suspend fun getSummary(
        @Query("month") month: String? = null
    ): BaseResponse<BudgetSummaryDto>

    @POST("budgets")
    suspend fun addBudget(@Body request: NewBudgetRequestDto): BaseResponse<BudgetDto>

    @PUT("budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: String,
        @Body request: UpdateBudgetRequestDto
    ): BaseResponse<BudgetDto>

    @DELETE("budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String): BaseResponse<Unit>

    @GET("budgets/recommendations")
    suspend fun getAIRecommendations(
        @Query("monthlyIncome") monthlyIncome: Double? = null
    ): BaseResponse<FinancialRecommendationResponseDto>

    @POST("budgets/apply-recommendations")
    suspend fun applyRecommendations(
        @Body request: ApplyRecommendationsRequest
    ): BaseResponse<ApplyRecommendationsResponseDto>
}
