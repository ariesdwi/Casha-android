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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<CashflowEntry> = emptyList(),
    val rawTransactions: List<TransactionCasha> = emptyList(), // Keep for edit/delete access
    val categories: List<CategoryCasha> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getCashflowHistoryUseCase: GetCashflowHistoryUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val syncTransactionsUseCase: SyncTransactionsUseCase,
    private val categorySyncUseCase: CategorySyncUseCase
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
                // Fetch current month history
                val response = getCashflowHistoryUseCase.execute(null, null, 1, 50)
                _uiState.update { it.copy(transactions = response.entries, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
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
                syncData()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
