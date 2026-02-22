package com.casha.app.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.auth.AuthManager
import com.casha.app.core.network.NetworkMonitor
import com.casha.app.domain.model.*
import com.casha.app.domain.usecase.auth.GetProfileUseCase
import com.casha.app.domain.usecase.dashboard.*
import com.casha.app.domain.usecase.goal.GetGoalsUseCase
import com.casha.app.domain.usecase.goal.GetGoalSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.casha.app.core.network.SyncEventBus
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class ChartTab {
    WEEK, MONTH
}

data class DashboardUiState(
    val recentTransactions: List<CashflowEntry> = emptyList(),
    val totalSpending: Double = 0.0,
    val report: SpendingReport = SpendingReport(0.0, 0.0, emptyList(), emptyList()),
    val cashflowSummary: CashflowSummary? = null,
    val unsyncedCount: Int = 0,
    val isOnline: Boolean = false,
    val isSyncing: Boolean = false,
    val selectedPeriod: SpendingPeriod = SpendingPeriod.THIS_MONTH,
    val nickname: String = "User",
    val goals: List<Goal> = emptyList(),
    val goalSummary: GoalSummary? = null,
    val selectedChartTab: ChartTab = ChartTab.WEEK,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase,
    private val getTotalSpendingUseCase: GetTotalSpendingUseCase,
    private val getSpendingReportUseCase: GetSpendingReportUseCase,
    private val getUnsyncTransactionCountUseCase: GetUnsyncTransactionCountUseCase,
    private val getCashflowHistoryUseCase: GetCashflowHistoryUseCase,
    private val getCashflowSummaryUseCase: GetCashflowSummaryUseCase,
    private val getGoalsUseCase: GetGoalsUseCase,
    private val getGoalSummaryUseCase: GetGoalSummaryUseCase,
    private val cashflowSyncUseCase: CashflowSyncUseCase,
    private val transactionSyncUseCase: TransactionSyncUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val authManager: AuthManager,
    private val networkMonitor: NetworkMonitor,
    private val syncEventBus: SyncEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var lastSyncAttempt = 0L
    private var lastDashboardRefresh = 0L

    init {
        setupNetworkMonitoring()
        setupSyncEventListener()
        loadInitialData()
    }

    private fun setupSyncEventListener() {
        viewModelScope.launch {
            syncEventBus.syncCompletedEvent.collect {
                // Instantly force UI state refresh when global event received
                refreshDashboard(force = true)
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val profile = getProfileUseCase()
                val firstName = profile.name.trim().split("\\s+".toRegex()).firstOrNull() ?: "User"
                _uiState.update { it.copy(nickname = firstName) }
            } catch (e: Exception) {
                // If profile fails, we still want the dashboard to load with default "User"
                _uiState.update { it.copy(errorMessage = "Profile load failed: ${e.message}") }
            }
            refreshDashboard(force = true)
        }
    }

    fun refreshDashboard(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastDashboardRefresh < 5000) return
        lastDashboardRefresh = now

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
            
            val period = _uiState.value.selectedPeriod
            val calendar = Calendar.getInstance()
            val monthStr = when (period) {
                SpendingPeriod.THIS_MONTH -> SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
                SpendingPeriod.LAST_MONTH -> {
                    calendar.add(Calendar.MONTH, -1)
                    SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
                }
                is SpendingPeriod.CUSTOM -> SimpleDateFormat("yyyy-MM", Locale.US).format(period.start)
                else -> null
            }
            val yearStr = when (period) {
                SpendingPeriod.THIS_YEAR -> calendar.get(Calendar.YEAR).toString()
                else -> null
            }

            try {
                if (_uiState.value.isOnline) {
                    cashflowSyncUseCase.syncAndFetch()
                }

                val spendingTask = async { getTotalSpendingUseCase.execute(period) }
                val reportsTask = async { getSpendingReportUseCase.execute() }
                val unsyncedTask = async { getUnsyncTransactionCountUseCase.execute() }
                
                val historyTask = async {
                    if (_uiState.value.isOnline) {
                        getCashflowHistoryUseCase.execute(monthStr, yearStr, 1, 5).entries
                    } else {
                        cashflowSyncUseCase.loadFromLocal(Date(), Date()).take(5)
                    }
                }
                
                val summaryTask = async {
                    if (_uiState.value.isOnline) {
                        getCashflowSummaryUseCase.execute(monthStr, yearStr)
                    } else {
                        cashflowSyncUseCase.calculateSummaryFromLocal(Date(), Date(), monthStr ?: yearStr ?: "Current")
                    }
                }
                
                val goalsTask = async { getGoalsUseCase.execute() }
                val goalSummaryTask = async { getGoalSummaryUseCase.execute() }

                _uiState.update { it.copy(
                    totalSpending = spendingTask.await(),
                    report = reportsTask.await().firstOrNull() ?: it.report,
                    unsyncedCount = unsyncedTask.await(),
                    recentTransactions = historyTask.await(),
                    cashflowSummary = summaryTask.await(),
                    goals = goalsTask.await(),
                    goalSummary = goalSummaryTask.await(),
                    isSyncing = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, errorMessage = "Dashboard refresh failed: ${e.message}") }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                cashflowSyncUseCase.syncAndFetch()
                refreshDashboard(force = true)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Sync failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun triggerAutoSync() {
        val now = System.currentTimeMillis()
        if (now - lastSyncAttempt < 30000) return
        lastSyncAttempt = now

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                transactionSyncUseCase.syncLocalTransactionsToRemote()
                syncData()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Auto-sync failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun changePeriod(period: SpendingPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        refreshDashboard(force = true)
    }

    fun changeChartTab(tab: ChartTab) {
        _uiState.update { it.copy(selectedChartTab = tab) }
    }

    private fun setupNetworkMonitoring() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (online) {
                    triggerAutoSync()
                }
            }
        }
    }
}
