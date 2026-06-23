package com.casha.app.ui.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.ChartCategorySpending
import com.casha.app.domain.model.DailySpending
import com.casha.app.domain.model.MonthlySpending
import com.casha.app.domain.model.ReportFilterPeriod
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.usecase.report.GetCategorySpendingUseCase
import com.casha.app.domain.usecase.report.GetDailySpendingUseCase
import com.casha.app.domain.usecase.report.GetMonthlySpendingUseCase
import com.casha.app.domain.usecase.report.GetTransactionByCategoryUseCase
import com.casha.app.domain.usecase.report.GetTransactionsByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.*
import javax.inject.Inject

data class ReportUiState(
    val categorySpendings: List<ChartCategorySpending> = emptyList(),
    val transactionsByCategory: List<TransactionCasha> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val monthlySpending: List<MonthlySpending> = emptyList(),
    val selectedDayTransactions: List<TransactionCasha> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPeriod: ReportFilterPeriod = ReportFilterPeriod.MONTH,
    val calendarDisplayMonth: YearMonth = YearMonth.now(),
    val calendarWeekStart: LocalDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY),
    val calendarDisplayYear: Int = LocalDate.now().year,
    val customStartDate: Date? = null,
    val customEndDate: Date? = null,
    val isPremium: Boolean = false,
    val showPaywall: Boolean = false,
    val showDaySheet: Boolean = false,
    val selectedDay: LocalDate? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val getTransactionByCategoryUseCase: GetTransactionByCategoryUseCase,
    private val getDailySpendingUseCase: GetDailySpendingUseCase,
    private val getMonthlySpendingUseCase: GetMonthlySpendingUseCase,
    private val getTransactionsByDateUseCase: GetTransactionsByDateUseCase,
    private val subscriptionManager: com.casha.app.core.auth.SubscriptionManager,
    private val syncEventBus: com.casha.app.core.network.SyncEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            subscriptionManager.isPremium.collect { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
            }
        }
        viewModelScope.launch {
            syncEventBus.syncCompletedEvent.collect {
                refreshAllData()
            }
        }
        refreshAllData()
    }

    // ── Filter ───────────────────────────────────────────────────────────────

    fun setFilter(period: ReportFilterPeriod) {
        _uiState.update { it.copy(
            selectedPeriod = period,
            customStartDate = null,
            customEndDate = null
        ) }
        refreshAllData()
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        val startDate = Date(startMillis)
        val endDate = Calendar.getInstance().apply {
            timeInMillis = endMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        _uiState.update { it.copy(
            selectedPeriod = ReportFilterPeriod.CUSTOM,
            customStartDate = startDate,
            customEndDate = endDate
        ) }
        refreshAllData()
    }

    // ── Calendar navigation ───────────────────────────────────────────────────

    /**
     * Navigate the calendar by [offset] units (±1).
     * Month mode → advance/retreat by 1 month.
     * Week mode  → advance/retreat by 7 days.
     * Year mode  → advance/retreat by 1 year.
     */
    fun navigatePeriod(offset: Int) {
        val s = _uiState.value
        when (s.selectedPeriod) {
            ReportFilterPeriod.MONTH -> {
                val next = s.calendarDisplayMonth.plusMonths(offset.toLong())
                _uiState.update { it.copy(calendarDisplayMonth = next) }
                loadCalendarData()
            }
            ReportFilterPeriod.WEEK -> {
                val next = s.calendarWeekStart.plusDays(offset * 7L)
                _uiState.update { it.copy(calendarWeekStart = next) }
                loadCalendarData()
            }
            ReportFilterPeriod.YEAR -> {
                val next = s.calendarDisplayYear + offset
                _uiState.update { it.copy(calendarDisplayYear = next) }
                loadCalendarData()
            }
            ReportFilterPeriod.CUSTOM -> { /* no calendar navigation in custom mode */ }
        }
    }

    /** Tap a day cell → load that day's transactions and open the bottom sheet. */
    fun selectDay(date: LocalDate) {
        _uiState.update { it.copy(selectedDay = date, showDaySheet = true, selectedDayTransactions = emptyList()) }
        viewModelScope.launch {
            val transactions = runCatching { getTransactionsByDateUseCase.execute(date) }.getOrElse { emptyList() }
            _uiState.update { it.copy(selectedDayTransactions = transactions) }
        }
    }

    fun dismissDaySheet() {
        _uiState.update { it.copy(showDaySheet = false, selectedDay = null, selectedDayTransactions = emptyList()) }
    }

    /** Tap a month tile in year view → drill down to that month. */
    fun onMonthTap(month: YearMonth) {
        _uiState.update { it.copy(
            selectedPeriod = ReportFilterPeriod.MONTH,
            calendarDisplayMonth = month
        ) }
        refreshAllData()
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    fun refreshAllData() {
        loadCategorySpending()
        loadCalendarData()
    }

    fun loadCategorySpending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val (start, end) = makeDateRange()
                val spendings = getCategorySpendingUseCase.execute(start, end)
                _uiState.update { it.copy(categorySpendings = spendings, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadCalendarData() {
        val s = _uiState.value
        viewModelScope.launch {
            try {
                when (s.selectedPeriod) {
                    ReportFilterPeriod.MONTH, ReportFilterPeriod.WEEK, ReportFilterPeriod.CUSTOM -> {
                        val (start, end) = makeCalendarDateRange()
                        val daily = getDailySpendingUseCase.execute(start, end)
                        _uiState.update { it.copy(dailySpending = daily) }
                    }
                    ReportFilterPeriod.YEAR -> {
                        val yearStart = Calendar.getInstance().apply {
                            set(s.calendarDisplayYear, Calendar.JANUARY, 1, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                        val yearEnd = Calendar.getInstance().apply {
                            set(s.calendarDisplayYear, Calendar.DECEMBER, 31, 23, 59, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.time
                        val monthly = getMonthlySpendingUseCase.execute(yearStart, yearEnd)
                        _uiState.update { it.copy(monthlySpending = monthly) }
                    }
                }
            } catch (_: Exception) { /* silently ignore calendar load failures */ }
        }
    }

    fun loadTransactionsByCategory(category: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val (start, end) = makeDateRange()
                val transactions = getTransactionByCategoryUseCase.execute(category, start, end)
                _uiState.update { it.copy(transactionsByCategory = transactions, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onCategoryClicked(category: String, navigate: (String) -> Unit) {
        if (_uiState.value.isPremium) {
            navigate(category)
        } else {
            _uiState.update { it.copy(showPaywall = true) }
        }
    }

    fun dismissPaywall() {
        _uiState.update { it.copy(showPaywall = false) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Date range for category spending & category transactions. */
    private fun makeDateRange(): Pair<Date, Date> {
        val s = _uiState.value
        val now = Date()
        if (s.selectedPeriod == ReportFilterPeriod.CUSTOM) {
            return Pair(s.customStartDate ?: now, s.customEndDate ?: now)
        }
        val calendar = Calendar.getInstance()
        val startDate: Date = when (s.selectedPeriod) {
            ReportFilterPeriod.WEEK -> calendar.apply {
                time = now
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time
            ReportFilterPeriod.MONTH -> calendar.apply {
                time = now
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time
            ReportFilterPeriod.YEAR -> calendar.apply {
                time = now
                set(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time
            else -> now
        }
        return Pair(startDate, now)
    }

    /** Date range scoped to the currently displayed calendar window. */
    private fun makeCalendarDateRange(): Pair<Date, Date> {
        val s = _uiState.value
        val cal = Calendar.getInstance()
        return when (s.selectedPeriod) {
            ReportFilterPeriod.MONTH -> {
                val ym = s.calendarDisplayMonth
                val start = cal.apply {
                    set(ym.year, ym.monthValue - 1, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
                }.time
                val end = cal.apply {
                    set(ym.year, ym.monthValue - 1, ym.lengthOfMonth(), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                Pair(start, end)
            }
            ReportFilterPeriod.WEEK -> {
                val ws = s.calendarWeekStart
                val start = cal.apply {
                    set(ws.year, ws.monthValue - 1, ws.dayOfMonth, 0, 0, 0); set(Calendar.MILLISECOND, 0)
                }.time
                val we = ws.plusDays(6)
                val end = cal.apply {
                    set(we.year, we.monthValue - 1, we.dayOfMonth, 23, 59, 59); set(Calendar.MILLISECOND, 999)
                }.time
                Pair(start, end)
            }
            ReportFilterPeriod.CUSTOM -> Pair(s.customStartDate ?: Date(), s.customEndDate ?: Date())
            else -> Pair(Date(), Date())
        }
    }
}
