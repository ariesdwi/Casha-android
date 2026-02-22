package com.casha.app.domain.usecase.budget

import com.casha.app.data.remote.dto.ApplyRecommendationsRequest
import com.casha.app.domain.model.BudgetAIRecommendation
import com.casha.app.domain.model.BudgetCasha
import com.casha.app.domain.model.BudgetSummary
import com.casha.app.domain.model.NewBudgetRequest
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
     * Flow 1: RemoteRepo.fetchBudgets() → LocalRepo.mergeBudgets()
     */
    suspend fun syncAllBudgets(monthYear: String? = null) {
        val remoteBudgets = repository.fetchRemoteBudgets(monthYear)
        repository.clearLocalBudgets()
        repository.saveLocalBudgets(remoteBudgets)
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
                currency = "IDR",
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
