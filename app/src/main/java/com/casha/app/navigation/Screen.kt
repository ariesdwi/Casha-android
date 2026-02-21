package com.casha.app.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Profile : Screen("profile")
    data object GoalTracker : Screen("goal_tracker")
    data class GoalDetail(val goalId: String) : Screen("goal_tracker_detail/$goalId")
    data class TransactionDetail(val transactionId: String) : Screen("transaction_detail/$transactionId")
    data object UnsyncedInfo : Screen("unsynced_info")
}
