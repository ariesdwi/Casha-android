package com.casha.app.ui.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.network.NetworkMonitor
import com.casha.app.core.util.DateHelper
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.domain.model.*
import com.casha.app.domain.usecase.budget.*
import com.casha.app.domain.usecase.category.CategorySyncUseCase
import com.casha.app.data.remote.dto.ApplyRecommendationsRequest
import com.casha.app.data.remote.dto.RecommendedBudgetPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetUiState(
    val budgets: List<BudgetCasha> = emptyList(),
    val budgetSummary: BudgetSummary? = null,
    val categories: List<CategoryCasha> = emptyList(),
    val fixedExpenses: Map<String, Double> = emptyMap(),
    val financialRecommendationResponse: FinancialRecommendationResponse? = null,
    val currentMonthYear: String? = null,
    val isLoading: Boolean = false,
    val isOnline: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val addBudgetUseCase: AddBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val getBudgetSummaryUseCase: GetBudgetSummaryUseCase,
    private val getRecommendationsUseCase: GetBudgetRecommendationsUseCase,
    private val applyRecommendationsUseCase: ApplyBudgetRecommendationsUseCase,
    private val budgetSyncUseCase: BudgetSyncUseCase,
    private val categorySyncUseCase: CategorySyncUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        setupNetworkMonitoring()
        loadInitialData()
    }

    // ── Initial Load ──

    private fun loadInitialData() {
        viewModelScope.launch {
            // Default to current month
            val currentMonth = DateHelper.generateMonthYearOptions().firstOrNull() ?: ""
            _uiState.update { it.copy(currentMonthYear = currentMonth) }
            // Let the first network emission handle fetching data
        }
    }

    // ── Refresh / Fetch ──

    fun refreshBudgetData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val monthYear = _uiState.value.currentMonthYear
                ?: DateHelper.generateMonthYearOptions().firstOrNull()
                ?: ""

            try {
                // Await categories first (local read, background sync)
                val categoryResult = categorySyncUseCase.fetchCategories()
                val activeCategories = categoryResult.filter { it.isActive }

                // If online: sync remote → local
                if (_uiState.value.isOnline) {
                    try { 
                        budgetSyncUseCase.syncAllBudgets(monthYear) 
                    } catch (e: Exception) {
                        // Log or show error but continue to load from local
                        _uiState.update { it.copy(errorMessage = "Sync failed: ${e.message}") }
                    }
                }

                // Read budgets from local (after sync)
                val budgets = budgetSyncUseCase.fetchBudgets(monthYear)

                // Fetch summary: remote if online, otherwise calculate locally
                val summary = if (_uiState.value.isOnline) {
                    try {
                        getBudgetSummaryUseCase(monthYear)
                    } catch (_: Exception) {
                        budgetSyncUseCase.calculateLocalSummary(budgets)
                    }
                } else {
                    budgetSyncUseCase.calculateLocalSummary(budgets)
                }

                _uiState.update { it.copy(
                    budgets = budgets,
                    categories = activeCategories,
                    budgetSummary = summary,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to refresh budget data"
                ) }
            }
        }
    }

    // ── Change Month ──

    fun setMonth(monthYear: String) {
        _uiState.update { it.copy(currentMonthYear = monthYear) }
        refreshBudgetData()
    }

    // ── Add Budget ──

    fun addBudget(request: NewBudgetRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                budgetSyncUseCase.syncAddBudget(request)
                refreshBudgetData()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to add budget"
                ) }
            }
        }
    }

    // ── Update Budget ──

    fun updateBudget(request: NewBudgetRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val id = request.id
            if (id == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Budget ID is missing") }
                return@launch
            }

            try {
                budgetSyncUseCase.syncUpdateBudget(id, request)
                refreshBudgetData()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to update budget"
                ) }
            }
        }
    }

    // ── Delete Budget ──

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                budgetSyncUseCase.syncDeleteBudget(id)
                refreshBudgetData()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to delete budget"
                ) }
            }
        }
    }

    // ── Categories ──

    // Categories are now fetched inline during refreshBudgetData
    // to match iOS synchronization patterns.

    // ── AI Recommendations ──

    fun fetchRecommendations(monthlyIncome: Double, fixedExpenses: Map<String, Double>? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, fixedExpenses = fixedExpenses ?: emptyMap()) }
            try {
                val recommendationsResponse = getRecommendationsUseCase(monthlyIncome)
                _uiState.update { it.copy(
                    financialRecommendationResponse = recommendationsResponse,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to fetch recommendations"
                ) }
            }
        }
    }

    fun applyRecommendedBudgets(): Boolean {
        val recommendations = _uiState.value.financialRecommendationResponse?.recommendations
            ?.filter { it.suggestedAmount > 0 } ?: emptyList()

        if (recommendations.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No recommendations to apply") }
            return false
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val monthOptions = DateHelper.generateMonthYearOptions()
                val currentMonthRaw = _uiState.value.currentMonthYear ?: monthOptions.first()
                val monthDisplay = DateHelper.formatMonthYearDisplay(currentMonthRaw)

                val payload = ApplyRecommendationsRequest(
                    month = monthDisplay,
                    budgets = recommendations.map {
                        RecommendedBudgetPayload(
                            amount = it.suggestedAmount,
                            category = it.category
                        )
                    }
                )
                applyRecommendationsUseCase(payload)
                refreshBudgetData()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to apply recommendations"
                ) }
            }
        }
        return true
    }

    // ── Clear Data (Logout) ──

    fun clearData() {
        _uiState.update {
            BudgetUiState()
        }
    }

    // ── Dismiss Error ──

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Network Monitoring ──

    private fun setupNetworkMonitoring() {
        viewModelScope.launch {
            var isFirstEmission = true
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (online) {
                    // Auto-sync unsynced budgets when coming back online
                    try { budgetSyncUseCase.syncLocalBudgetsToRemote() } catch (_: Exception) {}
                }
                
                // On iOS, auto-refresh happens on online transitions
                // To avoid missing initial offline data load, trigger refresh
                // on the absolute first network state emission.
                if (online || isFirstEmission) {
                    refreshBudgetData()
                }
                isFirstEmission = false
            }
        }
    }
}
