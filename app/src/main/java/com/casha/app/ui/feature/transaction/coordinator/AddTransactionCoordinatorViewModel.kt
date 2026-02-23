package com.casha.app.ui.feature.transaction.coordinator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PresentationState {
    IDLE,
    ACTION_SHEET,
    MANUAL_ENTRY,
    CHAT,
    CAMERA,
    PHOTO_LIBRARY,
    UPGRADE_PROMPT
}

data class CoordinatorUiState(
    val presentationState: PresentationState = PresentationState.IDLE,
    val isPremium: Boolean = false // Mocked for now
)

@HiltViewModel
class AddTransactionCoordinatorViewModel @Inject constructor(
    private val subscriptionManager: com.casha.app.core.auth.SubscriptionManager,
    private val syncEventBus: com.casha.app.core.network.SyncEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoordinatorUiState())
    val uiState: StateFlow<CoordinatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            subscriptionManager.isPremium.collect { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
            }
        }
    }

    fun onAddClicked() {
        showActionSheet()
    }

    fun showActionSheet() {
        _uiState.update { it.copy(presentationState = PresentationState.ACTION_SHEET) }
    }

    fun onFeatureSelected(state: PresentationState) {
        // Temporarily open all features without premium check
        _uiState.update { it.copy(presentationState = state) }
    }

    fun refreshData() {
        viewModelScope.launch {
            syncEventBus.emitSyncCompleted()
        }
    }

    fun close() {
        _uiState.update { it.copy(presentationState = PresentationState.IDLE) }
    }
}
