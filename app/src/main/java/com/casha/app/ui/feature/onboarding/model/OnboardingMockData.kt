package com.casha.app.ui.feature.onboarding.model

import android.content.Context
import com.casha.app.R

import com.casha.app.domain.model.*
import java.util.Calendar
import java.util.Date

/**
 * Provides mock data for the onboarding flow using real domain models.
 */
object OnboardingMockData {

    // Helper to get dates
    private fun getRelativeDate(daysOffset: Int, monthsOffset: Int = 0): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
        calendar.add(Calendar.MONTH, monthsOffset)
        return calendar.time
    }

    // ── 3. Revelation Page Data ──
    fun mockDashboardSummary(context: Context) = CashflowSummary(
        totalIncome = 15000000.0,
        totalExpense = 12500000.0,
        netBalance = 2500000.0,
        periodLabel = context.getString(R.string.onboarding_mock_dashboard_period)
    )

    // ── 4. Transaction History Page Data ──
    fun mockTransactions(context: Context) = listOf(
        CashflowEntry(
            id = "t1",
            title = context.getString(R.string.onboarding_mock_trx_coffee),
            category = context.getString(R.string.onboarding_mock_category_food),
            amount = 55000.0,
            date = getRelativeDate(0),
            type = CashflowType.EXPENSE
        ),
        CashflowEntry(
            id = "t2",
            title = context.getString(R.string.onboarding_mock_trx_ride),
            category = context.getString(R.string.onboarding_mock_category_transport),
            amount = 25000.0,
            date = getRelativeDate(-1),
            type = CashflowType.EXPENSE
        ),
        CashflowEntry(
            id = "t3",
            title = context.getString(R.string.onboarding_mock_trx_freelance),
            category = context.getString(R.string.onboarding_mock_category_income),
            amount = 1500000.0,
            date = getRelativeDate(-2),
            type = CashflowType.INCOME
        )
    )

    // ── 5. Budget Tracker Page Data ──
    fun mockBudgets(context: Context) = listOf(
        BudgetCasha(
            id = "b1",
            amount = 6200000.0,
            spent = 4350000.0,
            remaining = 1850000.0,
            period = "2024-03", // Example period
            startDate = getRelativeDate(0, -1),
            endDate = getRelativeDate(30, -1),
            category = context.getString(R.string.onboarding_mock_budget_total),
            currency = context.getString(R.string.onboarding_mock_currency)
        ),
        BudgetCasha(
            id = "b2",
            amount = 3000000.0,
            spent = 2450000.0,
            remaining = 550000.0,
            period = "2024-03",
            startDate = getRelativeDate(0, -1),
            endDate = getRelativeDate(30, -1),
            category = context.getString(R.string.onboarding_mock_category_food),
            currency = context.getString(R.string.onboarding_mock_currency)
        ),
        BudgetCasha(
            id = "b3",
            amount = 1200000.0,
            spent = 1100000.0,
            remaining = 100000.0,
            period = "2024-03",
            startDate = getRelativeDate(0, -1),
            endDate = getRelativeDate(30, -1),
            category = context.getString(R.string.onboarding_mock_category_transport),
            currency = context.getString(R.string.onboarding_mock_currency)
        )
    )

    fun mockBudgetSummary(context: Context) = BudgetSummary(
        totalBudget = mockBudgets(context).sumOf { it.amount },
        totalSpent = mockBudgets(context).sumOf { it.spent },
        totalRemaining = mockBudgets(context).sumOf { it.remaining },
        currency = context.getString(R.string.onboarding_mock_currency)
    )

    // ── 6. Portfolio Page Data ──
    fun mockPortfolioSummary(context: Context) = PortfolioSummary(
        currency = context.getString(R.string.onboarding_mock_currency),
        totalAssets = 45000000.0,
        totalUnrealizedReturn = 2500000.0,
        totalReturnPercentage = 5.8,
        breakdown = emptyList(),
        assets = listOf(
            Asset(
                id = "a1",
                name = context.getString(R.string.onboarding_mock_asset_saving),
                type = AssetType.SAVINGS_ACCOUNT,
                amount = 10000000.0,
                currency = context.getString(R.string.onboarding_mock_currency),
                userId = "test",
                createdAt = Date(),
                updatedAt = Date(),
                returnPercentage = 0.5
            ),
            Asset(
                id = "a2",
                name = context.getString(R.string.onboarding_mock_asset_stock),
                type = AssetType.STOCK,
                amount = 25000000.0,
                currency = context.getString(R.string.onboarding_mock_currency),
                userId = "test",
                createdAt = Date(),
                updatedAt = Date(),
                returnPercentage = 12.5
            )
        ),
        lastUpdated = Date()
    )
    
    fun mockAssetSummaryList(context: Context) = mockPortfolioSummary(context).assets

    // ── 7. Liabilities Page Data ──
    fun mockLiabilitySummary(context: Context) = LiabilitySummary(
        totalDebt = 485200000.0,
        totalMonthlyPayment = 2200000.0,
        activeLoansCount = 2,
        liabilities = listOf(
            Liability(
                id = "l1",
                name = context.getString(R.string.onboarding_mock_liability_mortgage),
                principal = 480000000.0,
                currentBalance = 480000000.0,
                interestRate = 8.5,
                startDate = getRelativeDate(-365),
                endDate = getRelativeDate(365 * 14),
                monthlyPayment = 4500000.0,
                category = LiabilityCategory.MORTGAGE
            ),
            Liability(
                id = "l2",
                name = context.getString(R.string.onboarding_mock_liability_credit),
                principal = 5200000.0,
                currentBalance = 5200000.0,
                interestRate = 2.0,
                startDate = getRelativeDate(-30),
                endDate = getRelativeDate(30),
                monthlyPayment = 520000.0,
                category = LiabilityCategory.CREDIT_CARD
            )
        )
    )

    // ── 8. Goal Tracker Page Data ──
    fun mockGoalSummary(context: Context) = GoalSummary(
        totalGoals = 3,
        activeGoals = 2,
        completedGoals = 1,
        totalTarget = 1000000000.0,
        totalCurrent = 250000000.0,
        overallProgress = 0.25,
        nearestDeadline = NearestDeadline(context.getString(R.string.onboarding_mock_goal_house), getRelativeDate(30))
    )

    fun mockGoal(context: Context) = Goal(
        id = "g1",
        name = context.getString(R.string.onboarding_mock_goal_house),
        targetAmount = 1000000000.0,
        currentAmount = 250000000.0,
        category = GoalCategory("c1", context.getString(R.string.onboarding_mock_goal_category_home), "🏠", "blue"),
        status = GoalStatus.ACTIVE,
        progress = GoalProgress(0.25, 365, 5000000.0)
    )
}
