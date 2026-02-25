package com.casha.app.ui.feature.liability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.*
import com.casha.app.domain.usecase.liability.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class LiabilityState(
    val activeLiabilities: List<Liability> = emptyList(),
    val paidOffLiabilities: List<Liability> = emptyList(),
    // Keep old field for backward compatibility with detail screens
    val liabilities: List<Liability> = emptyList(),
    val liabilitySummary: LiabilitySummary? = null,
    val latestStatement: LiabilityStatement? = null,
    val statements: List<LiabilityStatement> = emptyList(),
    val unbilledTransactions: UnbilledTransactions? = null,
    val selectedStatementDetail: LiabilityStatement? = null,
    val liabilityInsights: LiabilityInsight? = null,
    val paymentHistory: List<LiabilityPayment> = emptyList(),
    val transactions: List<LiabilityTransaction> = emptyList(),
    val simulationResult: SimulatePayoffResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    // Computed properties for the list screen
    val totalBalance: Double
        get() = activeLiabilities.sumOf { it.currentBalance }

    val totalMonthlyInstallment: Double
        get() = activeLiabilities.sumOf { it.monthlyInstallment ?: 0.0 }

    val activeCount: Int
        get() = activeLiabilities.size

    val paidOffCount: Int
        get() = paidOffLiabilities.size

    val overdueLiabilities: List<Liability>
        get() = activeLiabilities.filter { it.isOverdue == true }

    val nonOverdueLiabilities: List<Liability>
        get() = activeLiabilities.filter { it.isOverdue != true }
}

@HiltViewModel
class LiabilityViewModel @Inject constructor(
    private val getLiabilitiesUseCase: GetLiabilitiesUseCase,
    private val getLiabilitySummaryUseCase: GetLiabilitySummaryUseCase,
    private val createLiabilityUseCase: CreateLiabilityUseCase,
    private val recordLiabilityPaymentUseCase: RecordLiabilityPaymentUseCase,
    private val getLatestStatementUseCase: GetLatestStatementUseCase,
    private val getAllStatementsUseCase: GetAllStatementsUseCase,
    private val getStatementDetailsUseCase: GetStatementDetailsUseCase,
    private val getUnbilledTransactionsUseCase: GetUnbilledTransactionsUseCase,
    private val getLiabilityInsightsUseCase: GetLiabilityInsightsUseCase,
    private val getLiabilityPaymentHistoryUseCase: GetLiabilityPaymentHistoryUseCase,
    private val getLiabilityTransactionsUseCase: GetLiabilityTransactionsUseCase,
    private val createLiabilityTransactionUseCase: CreateLiabilityTransactionUseCase,
    private val deleteLiabilityUseCase: DeleteLiabilityUseCase,
    private val addInstallmentUseCase: AddInstallmentUseCase,
    private val simulatePayoffUseCase: SimulatePayoffUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiabilityState())
    val uiState: StateFlow<LiabilityState> = _uiState.asStateFlow()

    // MARK: - Cache Management
    private var lastFetchTime: Date? = null
    private val cacheValidityDuration: Long = 300_000 // 5 minutes in milliseconds

    private val isFresh: Boolean
        get() {
            val lastFetch = lastFetchTime ?: return false
            return (Date().time - lastFetch.time) < cacheValidityDuration
        }

    // MARK: - Fetch All Liabilities (ACTIVE + PAID_OFF)
    fun fetchAllLiabilities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val activeDeferred = async {
                    getLiabilitiesUseCase.execute(
                        status = "ACTIVE",
                        sortBy = "balance",
                        sortOrder = "desc"
                    )
                }
                val paidOffDeferred = async {
                    getLiabilitiesUseCase.execute(status = "PAID_OFF")
                }

                val active = activeDeferred.await()
                val paidOff = paidOffDeferred.await()

                _uiState.update {
                    it.copy(
                        activeLiabilities = active,
                        paidOffLiabilities = paidOff,
                        liabilities = active + paidOff,
                        isLoading = false
                    )
                }
                lastFetchTime = Date()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Legacy fetchLiabilities (for backward compat)
    fun fetchLiabilities() {
        fetchAllLiabilities()
    }

    // MARK: - Fetch Liability Summary (still available for other screens)
    fun fetchLiabilitySummary(force: Boolean = false) {
        if (!force && isFresh && _uiState.value.liabilitySummary != null) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val summary = getLiabilitySummaryUseCase.execute()
                lastFetchTime = Date()
                _uiState.update { it.copy(liabilitySummary = summary, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Create Liability
    fun createLiability(request: CreateLiabilityRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                createLiabilityUseCase.execute(request)
                // Refresh all liabilities
                fetchAllLiabilities()
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Record Payment
    fun recordPayment(
        liabilityId: String, 
        amount: Double, 
        paymentType: PaymentType, 
        principal: Double? = null,
        interest: Double? = null,
        notes: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val request = CreateLiabilityPaymentRequest(
                    amount = amount,
                    paymentDate = isoFormat.format(Date()),
                    paymentType = paymentType,
                    principalAmount = principal,
                    interestAmount = interest,
                    notes = notes
                )
                
                recordLiabilityPaymentUseCase.execute(liabilityId, request)
                
                // Refresh liabilities to get updated balances
                fetchAllLiabilities()
                
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to record payment", isLoading = false) }
            }
        }
    }

    // MARK: - Credit Card Specifics
    fun fetchLatestStatement(liabilityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val latest = getLatestStatementUseCase.execute(liabilityId)
                _uiState.update { it.copy(latestStatement = latest, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch latest statement", isLoading = false) }
            }
        }
    }

    fun fetchAllStatements(liabilityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val list = getAllStatementsUseCase.execute(liabilityId)
                _uiState.update { 
                    it.copy(
                        statements = list,
                        latestStatement = list.firstOrNull() ?: it.latestStatement,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch statements", isLoading = false) }
            }
        }
    }

    fun fetchStatementDetails(liabilityId: String, statementId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val detail = getStatementDetailsUseCase.execute(liabilityId, statementId)
                _uiState.update { it.copy(selectedStatementDetail = detail, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch statement detail", isLoading = false) }
            }
        }
    }

    fun fetchUnbilledTransactions(liabilityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val unbilled = getUnbilledTransactionsUseCase.execute(liabilityId)
                _uiState.update { it.copy(unbilledTransactions = unbilled, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch unbilled transactions", isLoading = false) }
            }
        }
    }

    fun fetchLiabilityInsights(liabilityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val insights = getLiabilityInsightsUseCase.execute(liabilityId)
                _uiState.update { it.copy(liabilityInsights = insights, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch insights", isLoading = false) }
            }
        }
    }

    fun fetchPaymentHistory(liabilityId: String, page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val history = getLiabilityPaymentHistoryUseCase.execute(liabilityId, page, pageSize)
                _uiState.update { it.copy(paymentHistory = history.payments, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch payment history", isLoading = false) }
            }
        }
    }

    fun fetchTransactions(liabilityId: String, page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val txs = getLiabilityTransactionsUseCase.execute(liabilityId, page, pageSize)
                _uiState.update { it.copy(transactions = txs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to fetch transactions", isLoading = false) }
            }
        }
    }

    fun createTransaction(
        liabilityId: String,
        name: String,
        amount: Double,
        categoryId: String,
        description: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val request = CreateLiabilityTransactionRequest(
                    name = name,
                    amount = amount,
                    categoryId = categoryId,
                    datetime = isoFormat.format(Date()),
                    description = description
                )
                
                createLiabilityTransactionUseCase.execute(liabilityId, request)
                
                // Refresh liabilities first to get updated balances
                fetchAllLiabilities()
                // Refresh unbilled transactions for the specific liability (credit cards)
                fetchUnbilledTransactions(liabilityId)
                
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add transaction", isLoading = false) }
            }
        }
    }

    // MARK: - Add Installment
    fun addInstallment(
        liabilityId: String,
        name: String,
        totalAmount: Double,
        monthlyAmount: Double,
        tenor: Int,
        currentMonth: Int,
        startDate: Date,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val request = CreateLiabilityInstallmentRequest(
                    name = name,
                    totalAmount = totalAmount,
                    monthlyAmount = monthlyAmount,
                    tenor = tenor,
                    currentMonth = currentMonth,
                    startDate = isoFormat.format(startDate)
                )
                
                addInstallmentUseCase.execute(liabilityId, request)
                
                // Refresh liability data to reflect new installment
                fetchAllLiabilities()
                
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add installment", isLoading = false) }
            }
        }
    }

    // MARK: - Simulate Payoff
    fun simulatePayoff(strategy: SimulationStrategy, additionalPayment: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, simulationResult = null) }
            try {
                val request = SimulatePayoffRequest(
                    strategy = strategy,
                    additionalPayment = additionalPayment.takeIf { it > 0 }
                )
                
                val result = simulatePayoffUseCase.execute(request)
                
                _uiState.update { it.copy(simulationResult = result, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to run simulation", isLoading = false) }
            }
        }
    }

    // MARK: - Delete Liability
    fun deleteLiability(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                deleteLiabilityUseCase.execute(id)
                
                // Refresh all liabilities
                fetchAllLiabilities()
                
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Clear State
    fun clearDetailState() {
        _uiState.update { 
            it.copy(
                latestStatement = null,
                statements = emptyList(),
                unbilledTransactions = null,
                selectedStatementDetail = null,
                liabilityInsights = null,
                paymentHistory = emptyList(),
                transactions = emptyList(),
                error = null
            )
        }
    }

    fun clearData() {
        _uiState.update { 
            LiabilityState() // Resets strictly to empty
        }
        lastFetchTime = null
    }

    // MARK: - Clear Error
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
