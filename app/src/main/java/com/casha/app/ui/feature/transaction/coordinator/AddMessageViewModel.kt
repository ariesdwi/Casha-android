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
import java.util.Date
import javax.inject.Inject

data class AddMessageUiState(
    val sentMessages: List<String> = emptyList(),
    val isSending: Boolean = false,
    val showConfirmation: Boolean = false,
    val transactionSuccess: Boolean = false,
    val aiResponseMessage: String = "",
    val lastIntent: String = ""
)

@HiltViewModel
class AddMessageViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val syncEventBus: SyncEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMessageUiState())
    val uiState: StateFlow<AddMessageUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    sentMessages = it.sentMessages + message,
                    isSending = true,
                    showConfirmation = false
                ) 
            }
            
            try {
                val result = chatRepository.parseChat(message)
                
                syncEventBus.emitSyncCompleted()
                
                _uiState.update {
                    it.copy(
                        isSending = false,
                        showConfirmation = true,
                        transactionSuccess = true,
                        aiResponseMessage = result.message,
                        lastIntent = result.intent.rawValue
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        showConfirmation = true,
                        transactionSuccess = false,
                        aiResponseMessage = e.localizedMessage ?: "Unknown error"
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
                    showConfirmation = false
                ) 
            }
            
            try {
                // MOCK DELAY AND RESPONSE FOR NOW
                delay(2000)
                
                _uiState.update {
                    it.copy(
                        isSending = false,
                        showConfirmation = true,
                        transactionSuccess = true,
                        aiResponseMessage = "Saved receipt for Rp150,000",
                        lastIntent = ChatParseIntent.EXPENSE.rawValue
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        showConfirmation = true,
                        transactionSuccess = false,
                        aiResponseMessage = e.localizedMessage ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { AddMessageUiState() }
    }
}
