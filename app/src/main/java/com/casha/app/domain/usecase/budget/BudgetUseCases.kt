package com.casha.app.domain.usecase.budget

import com.casha.app.data.remote.dto.ApplyRecommendationsRequest
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.BudgetRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

// ── Simple CRUD Use Cases ──

/**
 * Fetches budgets for a given period.
 */
class GetBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(month: String? = null): List<BudgetCasha> {
        return repository.fetchRemoteBudgets(month)
    }
}

/**
 * Creates a new budget (remote).
 */
class AddBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(request: NewBudgetRequest): BudgetCasha {
        return repository.createRemoteBudget(request)
    }
}

/**
 * Updates an existing budget (remote).
 */
class UpdateBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(id: String, request: NewBudgetRequest): BudgetCasha {
        return repository.updateRemoteBudget(id, request)
    }
}

/**
 * Deletes a budget by ID (remote).
 */
class DeleteBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteRemoteBudget(id)
    }
}

/**
 * Fetches aggregated budget summary (remote).
 */
class GetBudgetSummaryUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(month: String? = null): BudgetSummary {
        return repository.fetchRemoteSummary(month)
    }
}

/**
 * Fetches AI-powered budget recommendations.
 */
class GetBudgetRecommendationsUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(monthlyIncome: Double? = null): com.casha.app.domain.model.FinancialRecommendationResponse {
        return repository.fetchAIRecommendations(monthlyIncome)
    }
}

/**
 * Applies selected AI recommendations to create budgets.
 */
class ApplyBudgetRecommendationsUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(request: com.casha.app.data.remote.dto.ApplyRecommendationsRequest): List<BudgetCasha> {
        return repository.applyRemoteRecommendations(request)
    }
}

// ── Orchestrator Use Cases ──

/**
 * Budget sync orchestrator — mirrors iOS BudgetSyncUseCase.
 *
 * Handles:
 * 1. syncAllBudgets(month) — fetch remote → merge to local
 * 2. fetchBudgets(month) — read from local (after sync)
 * 3. syncAddBudget(request) — try remote, fallback to local offline
 * 4. syncUpdateBudget(id, request) — remote + local update
 * 5. syncDeleteBudget(id) — remote + local delete
 * 6. syncLocalBudgetsToRemote() — push unsynced → remote
 * 7. calculateLocalSummary(budgets) — sum from local data
 */
class BudgetSyncUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    /**
     * Sync remote budgets to local Room database.
     * Flow 1: RemoteRepo.fetchBudgetListData() → LocalRepo.mergeBudgets()
     * Returns [BudgetListData] so callers can display income/allocation info.
     */
    suspend fun syncAllBudgets(monthYear: String? = null): BudgetListData {
        val listData = repository.fetchRemoteBudgetListData(monthYear)
        repository.clearLocalBudgets()
        repository.saveLocalBudgets(listData.budgets)
        return listData
    }

    /**
     * Fetch budgets from local Room (after sync).
     * Flow 1 continued: LocalRepo.fetchAllBudgets() → filter by month
     */
    suspend fun fetchBudgets(monthYear: String? = null): List<BudgetCasha> {
        return repository.getLocalBudgets(monthYear)
    }

    /**
     * Add budget with offline fallback.
     * Flow 2: TRY remote → save to local (isSynced: true)
     *         CATCH → create local (isSynced: false)
     */
    suspend fun syncAddBudget(request: NewBudgetRequest) {
        try {
            // Try remote first
            val created = repository.createRemoteBudget(request)
            repository.saveLocalBudget(created)
        } catch (_: Exception) {
            // Offline fallback — create locally with isSynced: false
            val localBudget = BudgetCasha(
                id = UUID.randomUUID().toString(),
                amount = request.amount,
                spent = 0.0,
                remaining = request.amount,
                period = request.month,
                startDate = Date(),
                endDate = Date(),
                category = request.category,
                currency = com.casha.app.core.util.CurrencyFormatter.defaultCurrency,
                isSynced = false,
                createdAt = Date(),
                updatedAt = Date()
            )
            repository.saveLocalBudget(localBudget)
        }
    }

    /**
     * Update budget — remote + local.
     * Flow 3: RemoteRepo.updateBudget() → LocalRepo.updateBudget()
     */
    suspend fun syncUpdateBudget(id: String, request: NewBudgetRequest) {
        val updated = repository.updateRemoteBudget(id, request)
        repository.saveLocalBudget(updated)
    }

    /**
     * Delete budget — remote + local.
     * Flow 4: RemoteRepo.deleteBudget() → LocalRepo.deleteBudget()
     */
    suspend fun syncDeleteBudget(id: String) {
        repository.deleteRemoteBudget(id)
        repository.deleteLocalBudget(id)
    }

    /**
     * Push unsynced local budgets to remote.
     * Flow 6: getUnsyncedBudgets() → for each, POST remote → markAsSynced()
     */
    suspend fun syncLocalBudgetsToRemote() {
        val unsynced = repository.getUnsyncedBudgets()
        for (budget in unsynced) {
            try {
                val request = NewBudgetRequest(
                    amount = budget.amount,
                    month = budget.period,
                    category = budget.category
                )
                val created = repository.createRemoteBudget(request)
                repository.markAsSynced(budget.id, created.id)
            } catch (_: Exception) {
                // Skip this budget; will retry next sync
            }
        }
    }

    /**
     * Calculate summary from local budgets when offline.
     */
    fun calculateLocalSummary(budgets: List<BudgetCasha>): BudgetSummary {
        return repository.calculateLocalSummary(budgets)
    }
}

/**
 * Recalculates spent amounts for all budgets by re-syncing from remote.
 */
class RecalculateBudgetSpentUseCase @Inject constructor(
    private val budgetSyncUseCase: BudgetSyncUseCase
) {
    suspend operator fun invoke(month: String? = null) {
        budgetSyncUseCase.syncAllBudgets(month)
    }
}

/**
 * Calculates budget alerts using smart threshold logic.
 *
 * Smart threshold adjusts based on month progress:
 * - threshold = min(monthElapsedFraction + 0.10, 0.90)
 * - Example: Day 15 of 30 → threshold = 0.60 (60%)
 * - Prevents early-month false alarms
 *
 * Returns max 2 budget alerts sorted by severity (highest % used first).
 *
 * **Flow:**
 * 1. Try to fetch fresh budgets from remote (if online)
 * 2. Save to local database for caching
 * 3. Calculate alerts from local data
 * 4. If remote fetch fails, fall back to cached local data
 */
class GetBudgetAlertsUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(month: String? = null): List<BudgetCasha> {
        // Try to fetch fresh data from remote first (if online)
        try {
            val remoteBudgets = repository.fetchRemoteBudgets(month)
            if (remoteBudgets.isNotEmpty()) {
                // Save to local for caching
                repository.clearLocalBudgets()
                repository.saveLocalBudgets(remoteBudgets)
            }
        } catch (e: Exception) {
            // Remote fetch failed (offline or network error)
            // Will fall back to local cache below
        }
        
        // Fetch budgets from local database (either just synced or cached)
        val budgets = repository.getLocalBudgets(month)
        
        // Calculate smart threshold
        val calendar = java.util.Calendar.getInstance()
        val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val monthElapsedFraction = dayOfMonth.toDouble() / daysInMonth.toDouble()
        val threshold = kotlin.math.min(monthElapsedFraction + 0.10, 0.90)
        
        // Filter budgets exceeding threshold
        return budgets
            .filter { budget ->
                budget.amount > 0 && (budget.spent / budget.amount) > threshold
            }
            .sortedByDescending { budget ->
                budget.spent / budget.amount
            }
            .take(2)
    }
}
