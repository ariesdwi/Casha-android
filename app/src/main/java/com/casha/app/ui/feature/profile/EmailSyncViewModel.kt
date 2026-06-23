package com.casha.app.ui.feature.profile

import com.casha.app.core.util.AppEvents
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.EmailSyncStatus
import com.casha.app.domain.repository.EmailSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailSyncUiState(
    val status: EmailSyncStatus? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class EmailSyncViewModel @Inject constructor(
    private val emailSyncRepository: EmailSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailSyncUiState())
    val uiState: StateFlow<EmailSyncUiState> = _uiState.asStateFlow()

    init {
        loadStatus()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            emailSyncRepository.getStatus()
                .onSuccess { status ->
                    _uiState.update { it.copy(status = status, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    AppEvents.showSnackbar(error.message ?: "Gagal memuat status")
                }
        }
    }

    fun connectEmail(serverAuthCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            emailSyncRepository.connectOAuth(serverAuthCode)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    AppEvents.showSnackbar("Gmail berhasil terhubung ✓")
                    loadStatus()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    AppEvents.showSnackbar(error.message ?: "Gagal menghubungkan Gmail")
                }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            emailSyncRepository.disconnect()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            status = it.status?.copy(connected = false, emailAddress = null)
                        )
                    }
                    AppEvents.showSnackbar("Gmail diputus")
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    AppEvents.showSnackbar(error.message ?: "Gagal memutus koneksi")
                }
        }
    }

    fun clearMessages() { /* no-op — messages handled by AppEvents */ }
}
