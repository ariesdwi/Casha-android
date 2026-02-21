package com.casha.app.ui.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.TransactionCasha
import com.casha.app.domain.model.TransactionRequest
import com.casha.app.domain.usecase.transaction.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<TransactionCasha> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val syncTransactionsUseCase: SyncTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
        syncData() // Initial fetch
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getTransactionsUseCase().collect { transactions ->
                _uiState.update { it.copy(transactions = transactions, isLoading = false) }
            }
        }
    }

    fun loadTransactions() {
        // Redundant with observeTransactions, but kept for compatibility or forced refresh
        syncData()
    }

    fun addTransaction(request: TransactionRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                addTransactionUseCase(request)
                loadTransactions()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateTransaction(transaction: TransactionCasha) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                updateTransactionUseCase(transaction)
                loadTransactions()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteTransactionUseCase(id)
                loadTransactions()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                syncTransactionsUseCase()
                loadTransactions()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
