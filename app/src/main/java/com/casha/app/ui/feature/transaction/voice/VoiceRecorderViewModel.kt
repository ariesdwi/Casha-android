package com.casha.app.ui.feature.transaction.voice

import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.domain.model.ChatParseResult
import com.casha.app.domain.repository.ChatRepository
import com.casha.app.core.network.SyncEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VoicePhase {
    PERMISSION_REQUEST,
    PERMISSION_DENIED,
    IDLE,
    RECORDING,
    STOPPED,
    SENDING,
    ERROR
}

data class VoiceUiState(
    val phase: VoicePhase = VoicePhase.IDLE,
    val transcript: String = "",
    val errorMessage: String? = null,
    val selectedLocaleId: String = "en-US"
)

@HiltViewModel
class VoiceRecorderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository,
    private val syncEventBus: SyncEventBus
) : ViewModel(), RecognitionListener {

    // Shared success result so the composable can consume it once
    private val _parseSuccess = MutableStateFlow<ChatParseResult?>(null)
    val parseSuccess: StateFlow<ChatParseResult?> = _parseSuccess.asStateFlow()

    fun clearParseSuccess() { _parseSuccess.value = null }

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    init {
        initializeRecognizer()
    }

    fun requestPermissions() {
        _uiState.update { it.copy(phase = VoicePhase.PERMISSION_REQUEST) }
    }

    fun onPermissionsResult(granted: Boolean) {
        if (granted) {
            _uiState.update { it.copy(phase = VoicePhase.IDLE) }
        } else {
            _uiState.update { it.copy(phase = VoicePhase.PERMISSION_DENIED) }
        }
    }

    fun startRecording() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _uiState.update { 
                it.copy(
                    phase = VoicePhase.ERROR, 
                    errorMessage = "Speech recognition is not available on this device"
                ) 
            }
            return
        }

        initializeRecognizer()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, _uiState.value.selectedLocaleId)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Give the user 10 s of silence before the system auto-stops
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10_000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10_000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3_000L)
        }

        requestAudioFocus()
        _uiState.update { it.copy(phase = VoicePhase.RECORDING, transcript = "", errorMessage = null) }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(phase = VoicePhase.ERROR, errorMessage = "Failed to start recording: ${e.localizedMessage}") 
            }
        }
    }

    fun stopRecording() {
        if (_uiState.value.phase == VoicePhase.RECORDING) {
            abandonAudioFocus()
            speechRecognizer?.stopListening()
            // We do not transition phase immediately. Wait for onResults to yield the final transcript
            // and then transition to STOPPED. 
        }
    }

    fun reset() {
        abandonAudioFocus()
        tearDown()
        _uiState.update { 
            it.copy(
                phase = VoicePhase.IDLE, 
                transcript = "", 
                errorMessage = null
            ) 
        }
    }

    fun switchLocale(localeId: String) {
        if (_uiState.value.phase != VoicePhase.RECORDING) {
            _uiState.update { it.copy(selectedLocaleId = localeId) }
            tearDown()
            initializeRecognizer()
        }
    }

    fun setPhase(phase: VoicePhase) {
        _uiState.update { it.copy(phase = phase) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(phase = VoicePhase.ERROR, errorMessage = message) }
    }

    /** Called automatically when phase transitions to STOPPED with a non-empty transcript. */
    fun sendTranscriptToAI() {
        val transcript = _uiState.value.transcript
        if (transcript.isBlank()) {
            reset()
            return
        }
        _uiState.update { it.copy(phase = VoicePhase.SENDING, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = chatRepository.parseChat(transcript)
                syncEventBus.emitSyncCompleted()
                _parseSuccess.value = result
                // Phase stays SENDING until composable consumes parseSuccess and closes
            } catch (e: Exception) {
                val msg = when {
                    e is java.net.UnknownHostException || e is java.io.IOException ->
                        "No internet connection. Please check your network."
                    e is java.net.SocketTimeoutException ->
                        "Server took too long. Tap to try again."
                    else -> e.localizedMessage?.take(140) ?: "Something went wrong."
                }
                _uiState.update { it.copy(phase = VoicePhase.ERROR, errorMessage = msg) }
            }
        }
    }

    private fun initializeRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceRecorderViewModel)
            }
        }
    }

    private fun tearDown() {
        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        speechRecognizer = null
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE).build()
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tearDown()
    }

    // --- RecognitionListener Implementation ---

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }

        // If nothing was caught, silently reset to idle
        if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            if (_uiState.value.transcript.isNotEmpty()) {
                _uiState.update { it.copy(phase = VoicePhase.STOPPED) }
            } else {
                reset()
            }
        } else {
            _uiState.update { it.copy(phase = VoicePhase.ERROR, errorMessage = message) }
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val finalTranscript = matches?.firstOrNull() ?: ""
        
        if (finalTranscript.isNotEmpty() || _uiState.value.transcript.isNotEmpty()) {
            val transcriptToUse = finalTranscript.ifEmpty { _uiState.value.transcript }
            _uiState.update { 
                it.copy(
                    transcript = transcriptToUse,
                    phase = VoicePhase.STOPPED 
                ) 
            }
        } else {
            reset()
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull()
        if (!partial.isNullOrEmpty()) {
            _uiState.update { it.copy(transcript = partial) }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
