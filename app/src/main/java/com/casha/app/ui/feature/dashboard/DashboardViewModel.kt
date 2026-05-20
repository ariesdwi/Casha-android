package com.casha.app.ui.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.auth.AuthManager
import com.casha.app.core.auth.SubscriptionManager
import com.casha.app.core.network.NetworkMonitor
import com.casha.app.domain.model.*
import com.casha.app.domain.repository.IncomeRepository
import com.casha.app.domain.repository.TransactionRepository
import com.casha.app.domain.usecase.auth.GetProfileUseCase
import com.casha.app.domain.usecase.dashboard.*
import com.casha.app.domain.usecase.goal.GetGoalsUseCase
import com.casha.app.domain.usecase.goal.GetGoalSummaryUseCase
import com.casha.app.domain.usecase.wallet.GetWalletsUseCase
import com.casha.app.domain.usecase.wallet.GetWalletSummaryUseCase
import com.casha.app.widget.WidgetUpdater
import com.casha.app.widget.data.WidgetSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val errorMessage: String? = null,
    val wallets: List<Wallet> = emptyList(),
    val walletSummary: WalletSummary? = null,
    val defaultWalletId: String? = null
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
    private val getWalletsUseCase: GetWalletsUseCase,
    private val getWalletSummaryUseCase: GetWalletSummaryUseCase,
    private val cashflowSyncUseCase: CashflowSyncUseCase,
    private val transactionSyncUseCase: TransactionSyncUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val authManager: AuthManager,
    private val subscriptionManager: SubscriptionManager,
    private val networkMonitor: NetworkMonitor,
    private val syncEventBus: SyncEventBus,
    private val transactionRepository: TransactionRepository,
    private val incomeRepository: IncomeRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var lastSyncAttempt = 0L
    private var lastDashboardRefresh = 0L

    init {
        setupNetworkMonitoring()
        setupSyncEventListener()
        observeProfileChanges()
        observeDefaultWallet()
        loadInitialData()
    }

    private fun observeDefaultWallet() {
        viewModelScope.launch {
            authManager.defaultWalletId.collect { walletId ->
                _uiState.update { it.copy(defaultWalletId = walletId) }
            }
        }
    }

    private fun observeProfileChanges() {
        viewModelScope.launch {
            authManager.userName.collect { name ->
                if (!name.isNullOrBlank()) {
                    val firstName = name.trim().split("\\s+".toRegex()).firstOrNull() ?: "User"
                    _uiState.update { it.copy(nickname = firstName) }
                }
            }
        }
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
                authManager.saveProfileInfo(profile.name, profile.email, profile.avatar)
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
            refreshDashboardInternal()
        }
    }

    private suspend fun refreshDashboardInternal() {
        _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
            
            val period = _uiState.value.selectedPeriod
            val (startDate, endDate) = period.dateRange()

            if (period is com.casha.app.domain.model.SpendingPeriod.CUSTOM) {
                try {
                    val customSummary = cashflowSyncUseCase.calculateSummaryFromLocal(
                        startDate = period.start,
                        endDate = period.end,
                        monthLabel = getPeriodRawTitle(period)
                    )
                    val recentLocal = cashflowSyncUseCase.loadFromLocal(period.start, period.end).take(5)
                    val unsyncedTask = getUnsyncTransactionCountUseCase.execute()
                    val goalsTask = getGoalsUseCase.execute()
                    val goalSummaryTask = getGoalSummaryUseCase.execute()

                    _uiState.update { it.copy(
                        cashflowSummary = customSummary,
                        recentTransactions = recentLocal,
                        totalSpending = customSummary.totalExpense,
                        unsyncedCount = unsyncedTask,
                        goals = goalsTask,
                        goalSummary = goalSummaryTask,
                        isSyncing = false
                    ) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isSyncing = false, errorMessage = "Dashboard refresh failed: ${e.message}") }
                }
                return
            }
            
            // For remote fetching (if supported by server)
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            val monthStr = when (period) {
                SpendingPeriod.THIS_MONTH, SpendingPeriod.LAST_MONTH, is SpendingPeriod.CUSTOM -> {
                    SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
                }
                else -> null
            }
            val yearStr = when (period) {
                SpendingPeriod.THIS_YEAR -> calendar.get(Calendar.YEAR).toString()
                else -> null
            }
            // For UI display
            val periodLabel = getPeriodRawTitle(period)

            try {
                coroutineScope {
                    // Always sync from remote first when online
                    if (_uiState.value.isOnline) {
                        try { cashflowSyncUseCase.syncAndFetch() } catch (_: Exception) { }
                    }

                    val spendingTask = async { getTotalSpendingUseCase.execute(period) }
                    val reportsTask = async { getSpendingReportUseCase.execute() }
                    val unsyncedTask = async { getUnsyncTransactionCountUseCase.execute() }
                    
                    // Always read from local DB (populated by syncAndFetch above when online)
                    val historyTask = async {
                        cashflowSyncUseCase.loadFromLocal(startDate, endDate ?: Date()).take(5)
                    }
                    
                    val summaryTask = async {
                        // Only use remote API for periods it can filter (month/year params)
                        // For THIS_WEEK, LAST_THREE_MONTHS, ALL_TIME, FUTURE: always calculate locally
                        val canUseRemote = monthStr != null || yearStr != null
                        if (_uiState.value.isOnline && canUseRemote) {
                            try {
                                getCashflowSummaryUseCase.execute(monthStr, yearStr)
                            } catch (_: Exception) {
                                cashflowSyncUseCase.calculateSummaryFromLocal(startDate, endDate ?: Date(), periodLabel)
                            }
                        } else {
                            cashflowSyncUseCase.calculateSummaryFromLocal(startDate, endDate ?: Date(), periodLabel)
                        }
                    }
                    
                    val goalsTask = async { getGoalsUseCase.execute() }
                    val goalSummaryTask = async { getGoalSummaryUseCase.execute() }
                    val walletsTask = async { try { getWalletsUseCase.execute() } catch (_: Exception) { emptyList() } }
                    val walletSummaryTask = async { try { getWalletSummaryUseCase.execute() } catch (_: Exception) { null } }

                    _uiState.update { it.copy(
                        totalSpending = spendingTask.await(),
                        report = reportsTask.await().firstOrNull() ?: it.report,
                        unsyncedCount = unsyncedTask.await(),
                        recentTransactions = historyTask.await(),
                        cashflowSummary = summaryTask.await(),
                        goals = goalsTask.await(),
                        goalSummary = goalSummaryTask.await(),
                        wallets = walletsTask.await(),
                        walletSummary = walletSummaryTask.await(),
                        isSyncing = false
                    ) }

                    // Update widget with latest data
                    updateWidgetData()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, errorMessage = "Dashboard refresh failed: ${e.message}") }
            }
    }



    fun syncData() {
        viewModelScope.launch {
            syncDataInternal()
        }
    }

    private suspend fun syncDataInternal() {
        _uiState.update { it.copy(isSyncing = true) }
        try {
            cashflowSyncUseCase.syncAndFetch()
            refreshDashboardInternal()
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Sync failed: ${e.message}", isSyncing = false) }
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
                syncDataInternal()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Auto-sync failed: ${e.message}", isSyncing = false) }
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

    private fun getPeriodRawTitle(period: SpendingPeriod): String {
        return when (period) {
            SpendingPeriod.THIS_WEEK -> "This Week"
            SpendingPeriod.THIS_MONTH -> "This Month"
            SpendingPeriod.LAST_MONTH -> "Last Month"
            SpendingPeriod.LAST_THREE_MONTHS -> "Last 3 Months"
            SpendingPeriod.THIS_YEAR -> "This Year"
            SpendingPeriod.ALL_TIME -> "All Time"
            SpendingPeriod.FUTURE -> "Future"
            is SpendingPeriod.CUSTOM -> {
                val formatter = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.US)
                formatter.format(period.start)
            }
        }
    }

    private fun updateWidgetData() {
        viewModelScope.launch {
            val state = _uiState.value
            val summary = state.cashflowSummary ?: return@launch

            // Read actual premium state
            val isPremium = subscriptionManager.isPremium.firstOrNull() ?: false
            val isLoggedIn = authManager.accessToken.firstOrNull() != null

            WidgetUpdater.setAuthState(appContext, isLoggedIn = isLoggedIn, isPremium = isPremium)

            // Only write summary data if user is premium and logged in
            if (!isLoggedIn || !isPremium) return@launch

            val cal = Calendar.getInstance()
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val daysRemaining = daysInMonth - dayOfMonth

            // Compute today's spending using CUSTOM period
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val todayEnd = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time
            val spentToday = try {
                getTotalSpendingUseCase.execute(SpendingPeriod.CUSTOM(todayStart, todayEnd))
            } catch (_: Exception) { 0.0 }

            val budgetPct = if (summary.totalIncome > 0) {
                ((summary.totalExpense / summary.totalIncome) * 100).toInt().coerceIn(0, 999)
            } else 0

            val safeSpend = if (daysRemaining > 0 && summary.totalIncome > 0) {
                (summary.totalIncome - summary.totalExpense) / daysRemaining
            } else 0.0

            val status = when {
                summary.totalIncome <= 0 -> "no_income"
                budgetPct >= 100 -> "over_budget"
                budgetPct >= 75 -> "caution"
                else -> "comfortable"
            }
            val statusLabel = when (status) {
                "comfortable" -> "Aman"
                "caution" -> "Hati-hati"
                "over_budget" -> "Over Budget"
                "no_income" -> "Belum ada income"
                else -> "-"
            }

            val widgetSummary = WidgetSummary(
                safeSpendToday = safeSpend.coerceAtLeast(0.0),
                currency = summary.currency,
                daysRemaining = daysRemaining,
                monthlyIncome = summary.totalIncome,
                spentSoFar = summary.totalExpense,
                freeRemaining = (summary.totalIncome - summary.totalExpense).coerceAtLeast(0.0),
                status = status,
                statusLabel = statusLabel,
                budgetPctUsed = budgetPct,
                lastUpdatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
                spentToday = spentToday
            )

            WidgetUpdater.updateSummary(appContext, widgetSummary)
        }
    }
}
