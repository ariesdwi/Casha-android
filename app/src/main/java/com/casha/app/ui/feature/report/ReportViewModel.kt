package com.casha.app.ui.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.ChartCategorySpending
import com.casha.app.domain.model.ReportFilterPeriod
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.usecase.report.GetCategorySpendingUseCase
import com.casha.app.domain.usecase.report.GetTransactionByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ReportUiState(
    val categorySpendings: List<ChartCategorySpending> = emptyList(),
    val transactionsByCategory: List<TransactionCasha> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPeriod: ReportFilterPeriod = ReportFilterPeriod.MONTH,
    val customStartDate: Date? = null,
    val customEndDate: Date? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val getTransactionByCategoryUseCase: GetTransactionByCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        refreshAllData()
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

    fun refreshAllData() {
        loadCategorySpending()
    }

    private fun makeDateRange(): Pair<Date, Date> {
        val now = Date()
        val calendar = Calendar.getInstance()

        // Handle custom date range
        val state = _uiState.value
        if (state.selectedPeriod == ReportFilterPeriod.CUSTOM) {
            val start = state.customStartDate ?: now
            val end = state.customEndDate ?: now
            return Pair(start, end)
        }
        
        val startDate: Date = when (state.selectedPeriod) {
            ReportFilterPeriod.WEEK -> {
                calendar.apply {
                    time = now
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }
            ReportFilterPeriod.MONTH -> {
                calendar.apply {
                    time = now
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }
            ReportFilterPeriod.YEAR -> {
                calendar.apply {
                    time = now
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }
            ReportFilterPeriod.CUSTOM -> now // fallback, handled above
        }
        
        return Pair(startDate, now)
    }
}
