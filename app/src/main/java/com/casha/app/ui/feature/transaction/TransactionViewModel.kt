package com.casha.app.ui.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.CategoryCasha
import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.model.TransactionRequest
import com.casha.app.domain.usecase.category.CategorySyncUseCase
import com.casha.app.domain.usecase.dashboard.GetCashflowHistoryUseCase
import com.casha.app.domain.usecase.transaction.*
import com.casha.app.core.network.SyncEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.casha.app.domain.model.CashflowDateSection

import java.text.SimpleDateFormat
import java.util.*

data class TransactionUiState(
    val transactions: List<CashflowEntry> = emptyList(),
    val rawTransactions: List<TransactionCasha> = emptyList(),
    val categories: List<CategoryCasha> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // New fields for iOS alignment
    val selectedMonth: String = "This month",
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val cashflowSections: List<CashflowDateSection> = emptyList(),
    val filteredTransactions: List<CashflowDateSection> = emptyList()
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getCashflowHistoryUseCase: GetCashflowHistoryUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val syncTransactionsUseCase: SyncTransactionsUseCase,
    private val categorySyncUseCase: CategorySyncUseCase,
    private val syncEventBus: SyncEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
        fetchHistory()
        syncData()
        fetchCategories()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            getTransactionsUseCase().collect { transactions ->
                _uiState.update { it.copy(rawTransactions = transactions) }
            }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                var targetMonth: String? = null
                var targetYear: String? = null
                val selected = _uiState.value.selectedMonth
                
                when {
                    selected == "This month" -> {
                        targetMonth = com.casha.app.core.util.DateHelper.generateMonthYearOptions().firstOrNull()
                    }
                    selected == "This year" -> {
                        targetYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
                    }
                    selected.matches(Regex("\\d{4}-\\d{2}")) -> {
                        targetMonth = selected
                    }
                }

                // Fetch up to 100 for better client side grouping
                val response = getCashflowHistoryUseCase.execute(targetMonth, targetYear, 1, 100)
                val sections = groupTransactionsByDate(response.entries)
                _uiState.update { it.copy(
                    transactions = response.entries,
                    cashflowSections = sections,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }
    
    fun filterTransactionsByMonth(month: String) {
        _uiState.update { it.copy(selectedMonth = month) }
        // In a real app, you would pass the month/date range to getCashflowHistoryUseCase
        // For now, we simulate by fetching history again (which currently loads latest)
        fetchHistory()
    }

    fun searchTransactions(query: String) {
        val isSearching = query.isNotEmpty()
        val filtered = if (isSearching) {
            val matchedEntries = _uiState.value.transactions.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
            groupTransactionsByDate(matchedEntries)
        } else {
            emptyList()
        }
        
        _uiState.update { it.copy(
            searchQuery = query,
            isSearching = isSearching,
            filteredTransactions = filtered
        ) }
    }
    
    fun clearSearch() {
        _uiState.update { it.copy(
            searchQuery = "",
            isSearching = false,
            filteredTransactions = emptyList()
        ) }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                syncTransactionsUseCase()
                fetchHistory()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val result = categorySyncUseCase.fetchCategories()
                val activeCategories = result.filter { it.isActive }
                _uiState.update { it.copy(categories = activeCategories) }
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    fun addTransaction(request: TransactionRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                addTransactionUseCase(request)
                syncEventBus.emitSyncCompleted()
                syncData()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateTransaction(id: String, request: TransactionRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val existing = _uiState.value.rawTransactions.find { it.id == id }
                if (existing != null) {
                    val updated = existing.copy(
                        name = request.name,
                        category = request.category,
                        amount = request.amount,
                        datetime = request.datetime,
                        note = request.note
                    )
                    updateTransactionUseCase(updated)
                    syncEventBus.emitSyncCompleted()
                    syncData()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                deleteTransactionUseCase(id)
                syncEventBus.emitSyncCompleted()
                syncData()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun groupTransactionsByDate(transactions: List<CashflowEntry>): List<CashflowDateSection> {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val dayFormatter = SimpleDateFormat("EEEE", Locale.getDefault())
        
        val todayStr = dateFormatter.format(Date())
        val yesterdayStr = dateFormatter.format(Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time)

        return transactions
            .sortedByDescending { it.date }
            .groupBy { dateFormatter.format(it.date) }
            .map { (dateKey, entries) ->
                val parsed = dateFormatter.parse(dateKey) ?: Date()
                val displayDate = displayDateFormatter.format(parsed)
                val day = when (dateKey) {
                    todayStr -> "Today"
                    yesterdayStr -> "Yesterday"
                    else -> dayFormatter.format(parsed)
                }
                com.casha.app.domain.model.CashflowDateSection(day = day, date = displayDate, items = entries)
            }
    }
}
