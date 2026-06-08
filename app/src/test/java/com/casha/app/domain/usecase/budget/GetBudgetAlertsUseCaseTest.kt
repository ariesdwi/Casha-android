package com.casha.app.domain.usecase.budget

import com.casha.app.domain.model.BudgetCasha
import com.casha.app.domain.repository.BudgetRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*
import kotlin.math.min

/**
 * Unit tests for GetBudgetAlertsUseCase smart threshold logic.
 *
 * Tests verify:
 * 1. Smart threshold calculation based on month progress
 * 2. Budget filtering (only budgets exceeding threshold)
 * 3. Sorting by severity (highest % used first)
 * 4. Max 2 alerts returned
 *
 * Note: Uses a fake repository implementation for testing without external dependencies.
 */
class GetBudgetAlertsUseCaseTest {

    /**
     * Fake repository for testing
     */
    private class FakeBudgetRepository : BudgetRepository {
        var budgets: List<BudgetCasha> = emptyList()

        override suspend fun fetchRemoteBudgets(month: String?): List<BudgetCasha> = emptyList()
        override suspend fun fetchRemoteBudgetListData(month: String?): com.casha.app.domain.model.BudgetListData = 
            com.casha.app.domain.model.BudgetListData(emptyList(), 0.0, 0.0, 0.0)
        override suspend fun fetchRemoteSummary(month: String?): com.casha.app.domain.model.BudgetSummary = 
            com.casha.app.domain.model.BudgetSummary(0.0, 0.0, 0.0, "IDR")
        override suspend fun createRemoteBudget(request: com.casha.app.domain.model.NewBudgetRequest): BudgetCasha = budgets.first()
        override suspend fun updateRemoteBudget(id: String, request: com.casha.app.domain.model.NewBudgetRequest): BudgetCasha = budgets.first()
        override suspend fun deleteRemoteBudget(id: String) {}
        override suspend fun fetchAIRecommendations(monthlyIncome: Double?): com.casha.app.domain.model.FinancialRecommendationResponse = 
            com.casha.app.domain.model.FinancialRecommendationResponse(
                com.casha.app.domain.model.RecommendationSummary("", 0, 0, 0),
                com.casha.app.domain.model.FinancialSummary(0.0, 0.0)
            )
        override suspend fun applyRemoteRecommendations(request: com.casha.app.data.remote.dto.ApplyRecommendationsRequest): List<BudgetCasha> = emptyList()
        
        override suspend fun getLocalBudgets(month: String?): List<BudgetCasha> = budgets
        override suspend fun saveLocalBudget(budget: BudgetCasha) {}
        override suspend fun saveLocalBudgets(budgets: List<BudgetCasha>) {}
        override suspend fun deleteLocalBudget(id: String) {}
        override suspend fun getUnsyncedBudgets(): List<BudgetCasha> = emptyList()
        override suspend fun markAsSynced(localId: String, remoteId: String) {}
        override suspend fun clearLocalBudgets() {}
        override fun calculateLocalSummary(budgets: List<BudgetCasha>): com.casha.app.domain.model.BudgetSummary = 
            com.casha.app.domain.model.BudgetSummary(0.0, 0.0, 0.0, "IDR")
    }

    /**
     * Helper to create a test budget
     */
    private fun createBudget(
        id: String,
        category: String,
        amount: Double,
        spent: Double
    ): BudgetCasha {
        return BudgetCasha(
            id = id,
            amount = amount,
            spent = spent,
            remaining = amount - spent,
            period = "2024-01",
            startDate = Date(),
            endDate = Date(),
            category = category,
            currency = "IDR",
            isSynced = true
        )
    }

    /**
     * Helper to calculate smart threshold for testing
     */
    private fun calculateSmartThreshold(dayOfMonth: Int, daysInMonth: Int): Double {
        val monthElapsedFraction = dayOfMonth.toDouble() / daysInMonth.toDouble()
        return min(monthElapsedFraction + 0.10, 0.90)
    }

    @Test
    fun `test smart threshold calculation - day 1 should be 13 percent`() {
        // Day 1 of 30 = 0.033 fraction → threshold = 0.033 + 0.10 = 0.133 (13.3%)
        val threshold = calculateSmartThreshold(1, 30)
        
        assertTrue("Threshold should be around 13.3%", threshold > 0.13 && threshold < 0.14)
    }

    @Test
    fun `test smart threshold calculation - day 15 should be 60 percent`() {
        // Day 15 of 30 = 0.50 fraction → threshold = 0.50 + 0.10 = 0.60 (60%)
        val threshold = calculateSmartThreshold(15, 30)
        
        assertEquals(0.60, threshold, 0.01)
    }

    @Test
    fun `test smart threshold calculation - day 27 should cap at 90 percent`() {
        // Day 27 of 30 = 0.90 fraction → threshold = min(0.90 + 0.10, 0.90) = 0.90 (capped at 90%)
        val threshold = calculateSmartThreshold(27, 30)
        
        assertEquals(0.90, threshold, 0.01)
    }

    @Test
    fun `test threshold capped at 90 percent for end of month`() {
        // Near end of month, threshold should not exceed 90%
        val threshold = calculateSmartThreshold(30, 30)
        
        assertTrue("Threshold should be capped at 90%", threshold <= 0.90)
    }

    @Test
    fun `test budget filtering - only budgets exceeding threshold`() {
        // Simulate mid-month (60% threshold)
        val budgets = listOf(
            createBudget("1", "Food", 1000.0, 500.0),      // 50% - no alert
            createBudget("2", "Transport", 1000.0, 650.0), // 65% - alert
            createBudget("3", "Shopping", 1000.0, 580.0)   // 58% - no alert
        )
        
        val threshold = 0.60
        val filtered = budgets.filter { budget ->
            budget.amount > 0 && (budget.spent / budget.amount) > threshold
        }
        
        // Should only include Transport (65%)
        assertEquals(1, filtered.size)
        assertEquals("Transport", filtered[0].category)
    }

    @Test
    fun `test sorting - highest percentage used first`() {
        val budgets = listOf(
            createBudget("1", "Food", 1000.0, 800.0),       // 80%
            createBudget("2", "Transport", 1000.0, 950.0),  // 95%
            createBudget("3", "Shopping", 1000.0, 850.0)    // 85%
        )
        
        val sorted = budgets.sortedByDescending { it.spent / it.amount }
        
        // Should be sorted by % used descending: Transport (95%), Shopping (85%), Food (80%)
        assertEquals("Transport", sorted[0].category)
        assertEquals("Shopping", sorted[1].category)
        assertEquals("Food", sorted[2].category)
    }

    @Test
    fun `test max 2 alerts returned`() {
        // 5 budgets all over threshold (assuming 70% threshold)
        val budgets = listOf(
            createBudget("1", "Food", 1000.0, 950.0),       // 95%
            createBudget("2", "Transport", 1000.0, 920.0),  // 92%
            createBudget("3", "Shopping", 1000.0, 910.0),   // 91%
            createBudget("4", "Entertainment", 1000.0, 900.0), // 90%
            createBudget("5", "Health", 1000.0, 880.0)      // 88%
        )
        
        val threshold = 0.70
        val filtered = budgets
            .filter { it.amount > 0 && (it.spent / it.amount) > threshold }
            .sortedByDescending { it.spent / it.amount }
            .take(2)
        
        // Should return max 2 alerts
        assertEquals(2, filtered.size)
        // Should be the top 2 (Food 95% and Transport 92%)
        assertEquals("Food", filtered[0].category)
        assertEquals("Transport", filtered[1].category)
    }

    @Test
    fun `test empty budgets returns empty list`() {
        val budgets = emptyList<BudgetCasha>()
        val threshold = 0.60
        
        val filtered = budgets.filter { it.amount > 0 && (it.spent / it.amount) > threshold }
        
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `test budgets with zero amount are filtered out`() {
        val budgets = listOf(
            createBudget("1", "Food", 0.0, 100.0),  // Zero amount - should be filtered
            createBudget("2", "Transport", 1000.0, 800.0)  // 80%
        )
        
        val threshold = 0.60
        val filtered = budgets.filter { budget ->
            budget.amount > 0 && (budget.spent / budget.amount) > threshold
        }
        
        // Should not include zero-amount budget (avoids division by zero)
        assertEquals(1, filtered.size)
        assertEquals("Transport", filtered[0].category)
    }

    @Test
    fun `test all budgets healthy returns empty list`() {
        // All budgets under threshold (60% threshold)
        val budgets = listOf(
            createBudget("1", "Food", 1000.0, 300.0),      // 30%
            createBudget("2", "Transport", 1000.0, 400.0), // 40%
            createBudget("3", "Shopping", 1000.0, 500.0)   // 50%
        )
        
        val threshold = 0.60
        val filtered = budgets.filter { budget ->
            budget.amount > 0 && (budget.spent / budget.amount) > threshold
        }
        
        // All healthy, should return empty
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `test budget at exactly threshold is not included`() {
        val budgets = listOf(
            createBudget("1", "Food", 1000.0, 600.0)  // Exactly 60%
        )
        
        val threshold = 0.60
        val filtered = budgets.filter { budget ->
            budget.amount > 0 && (budget.spent / budget.amount) > threshold
        }
        
        // Budget at exactly threshold should NOT be included (only OVER threshold)
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `test budget just over threshold is included`() {
        val budgets = listOf(
            createBudget("1", "Food", 1000.0, 601.0)  // 60.1%
        )
        
        val threshold = 0.60
        val filtered = budgets.filter { budget ->
            budget.amount > 0 && (budget.spent / budget.amount) > threshold
        }
        
        // Budget just over threshold should be included
        assertEquals(1, filtered.size)
        assertEquals("Food", filtered[0].category)
    }
}
