package com.casha.app.ui.feature.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.usecase.auth.GetProfileUseCase
import com.casha.app.domain.usecase.dashboard.CashflowSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppLoadingUiState(
    val isSyncing: Boolean = true,
    val isComplete: Boolean = false,
    val destination: AppLoadingDestination? = null,
    val errorMessage: String? = null
)

enum class AppLoadingDestination {
    DASHBOARD,
    SETUP_CURRENCY
}

@HiltViewModel
class AppLoadingViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val cashflowSyncUseCase: CashflowSyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppLoadingUiState())
    val uiState: StateFlow<AppLoadingUiState> = _uiState.asStateFlow()

    init {
        loadAppData()
    }

    private fun loadAppData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, isComplete = false, errorMessage = null) }
            
            try {
                // Run tasks in parallel
                val profileDeferred = async { getProfileUseCase() }
                val syncDeferred = async { 
                    try {
                        cashflowSyncUseCase.syncAndFetch()
                    } catch (e: Exception) {
                        // Log sync error but don't necessarily crash the loading flow
                        null 
                    }
                }
                
                val profile = profileDeferred.await()
                syncDeferred.await()
                
                // Determine destination based on currency
                val isCurrencySet = !profile.currency.isNullOrBlank()
                
                val destination = if (isCurrencySet) {
                    AppLoadingDestination.DASHBOARD
                } else {
                    AppLoadingDestination.SETUP_CURRENCY
                }
                
                _uiState.update { it.copy(
                    isSyncing = false, 
                    isComplete = true,
                    destination = destination
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSyncing = false, 
                    errorMessage = e.message ?: "An unexpected error occurred during loading"
                ) }
            }
        }
    }
    
    fun retry() {
        loadAppData()
    }
}
