package com.casha.app.core.config

/**
 * Centralized API endpoint paths — Android equivalent of iOS `Endpoint.swift`.
 *
 * Since Retrofit uses annotations for HTTP methods, this object only stores paths.
 * Use these constants in Retrofit `@GET`, `@POST`, etc. annotations or when
 * building dynamic URLs.
 */
object ApiEndpoints {

    // ── Auth ──
    const val LOGIN = "auth/login"
    const val LOGIN_GOOGLE = "auth/google"
    const val FORGOT_PASSWORD = "auth/forgot-password"
    const val SIGNUP = "auth/signup"
    const val PROFILE = "auth/profile"
    const val PROFILE_UPDATE = "auth/profile/"
    const val PROFILE_DELETE = "auth/profile/"

    // ── Transactions ──
    const val TRANSACTION_CREATE = "transactions/create"
    const val TRANSACTION_LIST = "transactions"

    // ── Income ──
    const val INCOME_CREATE = "income"
    const val INCOME_LIST = "income"
    const val INCOME_SUMMARY = "income/summary"

    // ── Budgets ──
    const val BUDGET_LIST = "budgets/"
    const val BUDGET_SUMMARY = "budgets/summary"
    const val BUDGET_CREATE = "budgets/"
    fun budgetUpdate(id: String) = "budgets/$id"
    fun budgetDelete(id: String) = "budgets/$id"
    const val BUDGET_RECOMMENDATIONS = "budgets/recommendations"
    const val BUDGET_BULK_APPLY = "budgets/apply-recommendations"
    fun budgetApplyRecommendation(id: String) = "budget-recommendations/$id/apply"

    // ── Subscriptions ──
    const val SUBSCRIPTION_VERIFY = "subscriptions/verify"
    const val SUBSCRIPTION_STATUS = "subscriptions/status"
    const val SUBSCRIPTION_MOCK_MODE = "subscriptions/mock-mode"

    // ── Portfolio (Assets) ──
    const val ASSET_CREATE = "assets"
    const val ASSET_LIST = "assets"
    const val PORTFOLIO_SUMMARY = "assets/portfolio-summary"
    fun assetUpdate(id: String) = "assets/$id"
    fun assetDelete(id: String) = "assets/$id"
    fun assetTransactionCreate(assetId: String) = "assets/$assetId/transactions"
    fun assetTransactionList(assetId: String) = "assets/$assetId/transactions"

    // ── Liabilities ──
    const val LIABILITY_CREATE = "liabilities"
    const val LIABILITY_LIST = "liabilities"
    const val LIABILITY_SUMMARY = "liabilities/summary"
    fun liabilityDetails(id: String) = "liabilities/$id"
    fun liabilityDelete(id: String) = "liabilities/$id"
    fun liabilityPaymentRecord(liabilityId: String) = "liabilities/$liabilityId/payments"
    fun liabilityPaymentHistory(liabilityId: String) = "liabilities/$liabilityId/payments"
    fun liabilityTransactionCreate(liabilityId: String) = "liabilities/$liabilityId/transactions"
    fun liabilityStatementLatest(liabilityId: String) = "liabilities/$liabilityId/statements/latest"
    fun liabilityStatementAll(liabilityId: String) = "liabilities/$liabilityId/statements"
    fun liabilityStatementDetails(liabilityId: String, statementId: String) =
        "liabilities/$liabilityId/statements/$statementId"
    fun liabilityUnbilled(liabilityId: String) = "liabilities/$liabilityId/unbilled"
    fun liabilityInsights(liabilityId: String) = "liabilities/$liabilityId/insights"
    fun liabilityConvertToInstallment(liabilityId: String, transactionId: String) =
        "liabilities/$liabilityId/transactions/$transactionId/convert"

    // ── Goal Tracker ──
    const val GOAL_CREATE = "goals"
    const val GOAL_LIST = "goals"
    const val GOAL_SUMMARY = "goals/summary"
    const val GOAL_CATEGORIES = "goals/categories"
    const val GOAL_CATEGORY_CREATE = "goals/categories"
    fun goalDetails(id: String) = "goals/$id"
    fun goalUpdate(id: String) = "goals/$id"
    fun goalDelete(id: String) = "goals/$id"
    fun goalContributionAdd(id: String) = "goals/$id/contributions"
    fun goalContributionList(id: String) = "goals/$id/contributions"
    fun goalCategoryUpdate(id: String) = "goals/categories/$id"
    fun goalCategoryDelete(id: String) = "goals/categories/$id"
    fun goalCategoryGet(id: String) = "goals/categories/$id"

    // ── Categories ──
    const val CATEGORY_LIST = "categories"
    const val CATEGORY_CREATE = "categories"
    fun categoryUpdate(id: String) = "categories/$id"
    fun categoryDelete(id: String) = "categories/$id"
    fun categoryGet(id: String) = "categories/$id"

    // ── Chat (AI) ──
    const val CHAT_PARSE = "chat/parse"
    const val CHAT_PARSE_IMAGE = "chat/parse-image"

    // ── Cashflow Engine ──
    const val CASHFLOW_HISTORY = "cashflow/history"
    const val CASHFLOW_SUMMARY = "cashflow/summary"
    fun cashflowDelete(type: String, id: String) = "cashflow/$type/$id"
    fun cashflowUpdate(type: String, id: String) = "cashflow/$type/$id"
}
