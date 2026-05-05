package com.casha.app.ui.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.auth.AuthManager
import com.casha.app.core.network.SyncEventBus
import com.casha.app.domain.model.*
import com.casha.app.domain.usecase.wallet.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val getWalletsUseCase: GetWalletsUseCase,
    private val getWalletSummaryUseCase: GetWalletSummaryUseCase,
    private val setDefaultWalletUseCase: SetDefaultWalletUseCase,
    private val clearDefaultWalletUseCase: ClearDefaultWalletUseCase,
    private val addLiquidWalletUseCase: AddLiquidWalletUseCase,
    private val addCreditCardUseCase: AddCreditCardUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase,
    private val deleteWalletUseCase: DeleteWalletUseCase,
    private val transferWalletUseCase: TransferWalletUseCase,
    private val authManager: AuthManager,
    private val syncEventBus: SyncEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadAll()
        viewModelScope.launch {
            authManager.defaultWalletId.collect { walletId ->
                _uiState.update { it.copy(defaultWalletId = walletId) }
            }
        }
        viewModelScope.launch {
            syncEventBus.syncCompletedEvent.collect {
                loadAll()
            }
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val wallets = getWalletsUseCase.execute()
                val summary = getWalletSummaryUseCase.execute()
                _uiState.update {
                    it.copy(
                        wallets = wallets,
                        summary = summary,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    fun fetchWallets() {
        viewModelScope.launch {
            try {
                val wallets = getWalletsUseCase.execute()
                _uiState.update { it.copy(wallets = wallets) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun fetchSummary() {
        viewModelScope.launch {
            try {
                val summary = getWalletSummaryUseCase.execute()
                _uiState.update { it.copy(summary = summary) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun setDefault(walletId: String) {
        val previousDefault = _uiState.value.defaultWalletId
        // Optimistic update
        _uiState.update { it.copy(defaultWalletId = walletId) }

        viewModelScope.launch {
            try {
                setDefaultWalletUseCase.execute(walletId)
                authManager.setDefaultWalletId(walletId)
                _uiState.update { it.copy(successMessage = "Default wallet updated") }
            } catch (e: Exception) {
                // Rollback
                _uiState.update {
                    it.copy(defaultWalletId = previousDefault, errorMessage = e.message)
                }
            }
        }
    }

    fun clearDefault() {
        val previousDefault = _uiState.value.defaultWalletId
        _uiState.update { it.copy(defaultWalletId = null) }

        viewModelScope.launch {
            try {
                clearDefaultWalletUseCase.execute()
                authManager.setDefaultWalletId(null)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(defaultWalletId = previousDefault, errorMessage = e.message)
                }
            }
        }
    }

    fun addLiquidWallet(request: AddLiquidWalletRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val wallet = addLiquidWalletUseCase.execute(request)
                _uiState.update {
                    it.copy(
                        wallets = it.wallets + wallet,
                        isLoading = false,
                        successMessage = "Wallet added"
                    )
                }
                fetchSummary()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addCreditCard(request: AddCreditCardRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val wallet = addCreditCardUseCase.execute(request)
                _uiState.update {
                    it.copy(
                        wallets = it.wallets + wallet,
                        isLoading = false,
                        successMessage = "Credit card added"
                    )
                }
                fetchSummary()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateWallet(id: String, source: WalletSource, request: UpdateWalletRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val updated = updateWalletUseCase.execute(id, source, request)
                _uiState.update { state ->
                    state.copy(
                        wallets = state.wallets.map { if (it.id == id) updated else it },
                        isLoading = false,
                        successMessage = "Wallet updated"
                    )
                }
                fetchSummary()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteWallet(id: String, source: WalletSource) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteWalletUseCase.execute(id, source)
                _uiState.update { state ->
                    state.copy(
                        wallets = state.wallets.filter { it.id != id },
                        isLoading = false,
                        successMessage = "Wallet deleted"
                    )
                }
                fetchSummary()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun transferWallet(request: TransferWalletRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = transferWalletUseCase.execute(request)
                _uiState.update { state ->
                    state.copy(
                        wallets = state.wallets.map { wallet ->
                            when (wallet.id) {
                                result.from.id -> result.from
                                result.to.id -> result.to
                                else -> wallet
                            }
                        },
                        isLoading = false,
                        successMessage = "Transfer successful"
                    )
                }
                fetchSummary()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
