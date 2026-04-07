package com.casha.app.ui.feature.transaction.coordinator

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.ChatParseIntent
import com.casha.app.domain.model.ChatParseResult
import com.casha.app.domain.repository.ChatRepository
import com.casha.app.core.network.SyncEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import com.casha.app.core.utils.ImageUtils
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Date
import javax.inject.Inject

data class AddMessageUiState(
    val sentMessages: List<String> = emptyList(),
    val isSending: Boolean = false,
    val showConfirmation: Boolean = false,
    val transactionSuccess: Boolean = false,
    val aiResponseMessage: String = "",
    val lastIntent: String = "",
    // Non-blocking error — shown as a dismissible banner, NOT a blocking card
    val errorMessage: String? = null,
    val lastFailedImageUri: Uri? = null
)

@HiltViewModel
class AddMessageViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val syncEventBus: SyncEventBus,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMessageUiState())
    val uiState: StateFlow<AddMessageUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sentMessages = it.sentMessages + message,
                    isSending = true,
                    showConfirmation = false,
                    errorMessage = null
                )
            }

            try {
                val result = chatRepository.parseChat(message)
                val isSuccess = result.intent != ChatParseIntent.UNKNOWN
                if (isSuccess) {
                    syncEventBus.emitSyncCompleted()
                }
                _uiState.update {
                    it.copy(
                        isSending = false,
                        showConfirmation = true,
                        transactionSuccess = isSuccess,
                        aiResponseMessage = result.message,
                        lastIntent = result.intent.rawValue
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        showConfirmation = false,
                        errorMessage = friendlyError(e)
                    )
                }
            }
        }
    }

    fun sendImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    showConfirmation = false,
                    errorMessage = null,
                    lastFailedImageUri = null
                )
            }

            try {
                val tempFile = ImageUtils.compressImage(context, imageUri, "chat_upload.jpg")

                if (tempFile == null || !tempFile.exists()) {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = "Couldn't read the image. Please try selecting it again.",
                            lastFailedImageUri = imageUri
                        )
                    }
                    return@launch
                }

                val result = chatRepository.parseImage(tempFile)
                val isSuccess = result.intent != ChatParseIntent.UNKNOWN
                if (isSuccess) {
                    syncEventBus.emitSyncCompleted()
                }

                try { tempFile.delete() } catch (_: Exception) { /* ignore cleanup errors */ }

                _uiState.update {
                    it.copy(
                        isSending = false,
                        showConfirmation = true,
                        transactionSuccess = isSuccess,
                        aiResponseMessage = result.message,
                        lastIntent = result.intent.rawValue,
                        lastFailedImageUri = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        showConfirmation = false,
                        errorMessage = friendlyError(e),
                        lastFailedImageUri = imageUri
                    )
                }
            }
        }
    }

    /** Retry the last failed image upload. */
    fun retryLastImage() {
        val uri = _uiState.value.lastFailedImageUri ?: return
        sendImage(uri)
    }

    /** Dismiss the error banner without retrying. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, lastFailedImageUri = null) }
    }

    fun resetState() {
        _uiState.update { AddMessageUiState() }
    }

    private fun friendlyError(e: Exception): String = when (e) {
        is UnknownHostException, is IOException ->
            "No internet connection. Please check your network and try again."
        is SocketTimeoutException ->
            "The server took too long to respond. Please try again."
        else -> {
            val msg = e.localizedMessage ?: ""
            if (msg.length > 160) "Something went wrong. Please try again." else msg
        }
    }
}
