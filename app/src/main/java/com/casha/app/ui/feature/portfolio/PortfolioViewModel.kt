package com.casha.app.ui.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.*
import com.casha.app.domain.usecase.portfolio.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class PortfolioUiState(
    val assets: List<Asset> = emptyList(),
    val portfolioSummary: PortfolioSummary? = null,
    val totalExpenses: Double = 0.0,
    val assetTransactions: List<AssetTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val realBalance: Double
        get() = (portfolioSummary?.totalAssets ?: 0.0) - totalExpenses
}

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val getPortfolioSummaryUseCase: GetPortfolioSummaryUseCase,
    private val createAssetUseCase: CreateAssetUseCase,
    private val getAssetsUseCase: GetAssetsUseCase,
    private val updateAssetUseCase: UpdateAssetUseCase,
    private val deleteAssetUseCase: DeleteAssetUseCase,
    private val addAssetTransactionUseCase: AddAssetTransactionUseCase,
    private val getAssetTransactionsUseCase: GetAssetTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    // MARK: - Cache Management
    private var lastFetchTime: Date? = null
    private val cacheValidityDuration: Long = 300_000 // 5 minutes in ms

    private val isFresh: Boolean
        get() {
            val lastFetch = lastFetchTime ?: return false
            return (Date().time - lastFetch.time) < cacheValidityDuration
        }

    // MARK: - Fetch Assets
    fun fetchAssets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val assets = getAssetsUseCase.execute()
                _uiState.update { it.copy(assets = assets, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Fetch Portfolio Summary
    fun fetchPortfolioSummary(force: Boolean = false) {
        if (!force && isFresh && _uiState.value.portfolioSummary != null) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val summary = getPortfolioSummaryUseCase.execute()
                lastFetchTime = Date()
                _uiState.update { it.copy(portfolioSummary = summary, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Create Asset
    fun createAsset(request: CreateAssetRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val newAsset = createAssetUseCase.execute(request)
                _uiState.update {
                    it.copy(
                        assets = it.assets + newAsset,
                        isLoading = false
                    )
                }
                // Refresh portfolio summary
                fetchPortfolioSummary(force = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Update Asset
    fun updateAsset(id: String, request: UpdateAssetRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val updatedAsset = updateAssetUseCase.execute(id, request)
                _uiState.update { state ->
                    val updatedList = state.assets.map { if (it.id == id) updatedAsset else it }
                    state.copy(assets = updatedList, isLoading = false)
                }
                // Refresh portfolio summary
                fetchPortfolioSummary(force = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Delete Asset
    fun deleteAsset(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                deleteAssetUseCase.execute(id)
                _uiState.update { state ->
                    state.copy(
                        assets = state.assets.filter { it.id != id },
                        isLoading = false
                    )
                }
                // Refresh portfolio summary
                fetchPortfolioSummary(force = true)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Add Asset Transaction
    fun addAssetTransaction(assetId: String, request: CreateAssetTransactionRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                addAssetTransactionUseCase.execute(assetId, request)
                // Refresh asset, transactions, and portfolio summary for real-time update
                fetchAssets()
                fetchAssetTransactions(assetId)
                fetchPortfolioSummary(force = true)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Fetch Asset Transactions
    fun fetchAssetTransactions(assetId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val transactions = getAssetTransactionsUseCase.execute(assetId)
                _uiState.update { it.copy(assetTransactions = transactions, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // MARK: - Fetch Total Expenses
    fun fetchTotalExpenses() {
        // TODO: Implement actual expense fetching from transaction repository
        // For now, using a placeholder value as in Swift
        _uiState.update { it.copy(totalExpenses = 0.0) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearData() {
        _uiState.update { PortfolioUiState() }
        lastFetchTime = null
    }
}
