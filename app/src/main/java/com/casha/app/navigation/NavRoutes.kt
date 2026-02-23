package com.casha.app.navigation

/**
 * Defines all navigation routes in the Casha app.
 */
sealed class NavRoutes(val route: String) {

    // ── Auth ──
    data object Splash : NavRoutes("splash")
    data object Login : NavRoutes("login")
    data object Register : NavRoutes("register")
    data object ForgotPassword : NavRoutes("forgot_password")
    data object SetupCurrency : NavRoutes("setup_currency")

    // ── App Loading ──
    data object AppLoading : NavRoutes("app_loading")

    // ── Main Tabs ──
    data object Dashboard : NavRoutes("dashboard")
    data object Budget : NavRoutes("budget")
    data object Report : NavRoutes("report")
    data object ReportCategoryDetail : NavRoutes("report_category/{category}") {
        fun createRoute(category: String) = "report_category/$category"
    }

    // ── Budget Support ──
    data object BudgetDetail : NavRoutes("budget_detail/{budgetId}") {
        fun createRoute(budgetId: String) = "budget_detail/$budgetId"
    }

    // ── Transaction ──
    data object Transactions : NavRoutes("transactions")
    data object AddTransaction : NavRoutes("add_transaction")
    data object EditTransaction : NavRoutes("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: String) = "edit_transaction/$transactionId"
    }

    data object TransactionDetail : NavRoutes("transaction_detail/{transactionId}/{cashflowType}") {
        fun createRoute(transactionId: String, cashflowType: String) = "transaction_detail/$transactionId/$cashflowType"
    }

    // ── Goal Tracker ──
    data object GoalTracker : NavRoutes("goal_tracker")
    data object GoalTrackerDetail : NavRoutes("goal_tracker_detail/{goalId}") {
        fun createRoute(goalId: String) = "goal_tracker_detail/$goalId"
    }

    data object AddGoal : NavRoutes("add_goal")

    // ── Portfolio ──
    data object Portfolio : NavRoutes("portfolio")
    data object AssetDetail : NavRoutes("asset_detail/{assetId}") {
        fun createRoute(assetId: String) = "asset_detail/$assetId"
    }

    data object CreateAsset : NavRoutes("create_asset")
    data object EditAsset : NavRoutes("edit_asset/{assetId}") {
        fun createRoute(assetId: String) = "edit_asset/$assetId"
    }

    // ── Liabilities ──
    data object Liabilities : NavRoutes("liabilities")
    data object LiabilityDetail : NavRoutes("liability_detail/{liabilityId}") {
        fun createRoute(liabilityId: String) = "liability_detail/$liabilityId"
    }

    data object CreateLiability : NavRoutes("create_liability")

    // ── Income ──
    data object Income : NavRoutes("income")
    data object AddIncome : NavRoutes("add_income")

    // ── Profile ──
    data object Profile : NavRoutes("profile")
    data object ProfileEdit : NavRoutes("profile_edit")
    data object Categories : NavRoutes("categories")

    // ── Other ──
    data object Recommendation : NavRoutes("recommendation")
    data object Subscription : NavRoutes("subscription")
    data object Chat : NavRoutes("chat?imageUri={imageUri}") {
        fun createRoute(imageUri: String? = null) = if (imageUri != null) "chat?imageUri=$imageUri" else "chat"
    }
    data object ReceiptCamera : NavRoutes("receipt_camera")
}
