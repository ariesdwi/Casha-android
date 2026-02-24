package com.casha.app.ui.feature.transaction.coordinator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.casha.app.core.utils.ImageUtils
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
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
    val isPremium: Boolean = false, // Mocked for now
    val progressState: ProgressState = ProgressState(),
    val errorMessage: String? = null
)

@HiltViewModel
class AddTransactionCoordinatorViewModel @Inject constructor(
    private val subscriptionManager: com.casha.app.core.auth.SubscriptionManager,
    private val syncEventBus: com.casha.app.core.network.SyncEventBus,
    private val chatRepository: com.casha.app.domain.repository.ChatRepository,
    @ApplicationContext private val context: Context
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

    fun uploadImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    progressState = ProgressState(
                        isVisible = true,
                        currentProcessingStatus = "Analyzing Receipt...",
                        currentIcon = Icons.Default.CloudUpload,
                        processingProgress = 0f,
                        isCompleted = false
                    ),
                    errorMessage = null 
                ) 
            }
            
            // Loop to fake smooth progress up to 90%
            val progressJob = launch {
                var currentProgress = 0f
                while (currentProgress < 0.9f) {
                    delay(50)
                    currentProgress += 0.02f
                    _uiState.update { state ->
                        state.copy(
                            progressState = state.progressState.copy(processingProgress = currentProgress)
                        )
                    }
                }
            }
            
            try {
                // Compress URI to a temporary optimized JPEG file
                val tempFile = ImageUtils.compressImage(context, imageUri, "scan_go.jpg")
                
                if (tempFile == null || !tempFile.exists()) {
                    throw Exception("Failed to process image")
                }

                val result = chatRepository.parseImage(tempFile)
                syncEventBus.emitSyncCompleted()
                
                // Clean up temp file
                try { tempFile.delete() } catch (e: Exception) { /* ignore */ }
                
                progressJob.cancel()
                
                // Show success state
                _uiState.update { state -> 
                    state.copy(
                        progressState = state.progressState.copy(
                            processingProgress = 1f,
                            isCompleted = true,
                            currentIcon = Icons.Default.CheckCircle,
                            currentProcessingStatus = "Recording Transaction..."
                        )
                    ) 
                }
                
                delay(1200) // Brief pause to show success checkmark
                
                // On success, close the modal completely (Flow A)
                _uiState.update { 
                    it.copy(
                        progressState = ProgressState(), 
                        presentationState = PresentationState.IDLE
                    ) 
                }
            } catch (e: Exception) {
                progressJob.cancel()
                _uiState.update { 
                    it.copy(
                        progressState = ProgressState(), 
                        errorMessage = e.localizedMessage ?: "Failed to upload image"
                    ) 
                }
            }
        }
    }
}
