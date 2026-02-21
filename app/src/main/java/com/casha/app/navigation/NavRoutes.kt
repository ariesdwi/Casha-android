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
    data object TransactionList : NavRoutes("transaction_list")
    data object AddTransaction : NavRoutes("add_transaction")
    data object Budget : NavRoutes("budget")
    data object Report : NavRoutes("report")

    // ── Transaction Detail ──
    data object TransactionDetail : NavRoutes("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }

    data object TransactionEdit : NavRoutes("transaction_edit/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_edit/$transactionId"
    }

    // ── Budget Detail ──
    data object BudgetDetail : NavRoutes("budget_detail/{budgetId}") {
        fun createRoute(budgetId: String) = "budget_detail/$budgetId"
    }

    data object AddBudget : NavRoutes("add_budget")
    data object EditBudget : NavRoutes("edit_budget/{budgetId}") {
        fun createRoute(budgetId: String) = "edit_budget/$budgetId"
    }

    data object BudgetAIAdvisor : NavRoutes("budget_ai_advisor")

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

    // ── Other ──
    data object Recommendation : NavRoutes("recommendation")
    data object Subscription : NavRoutes("subscription")
}
