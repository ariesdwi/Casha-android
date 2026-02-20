package com.casha.app.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.auth.AuthManager
import com.casha.app.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val toastMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun register() {
        val state = _uiState.value
        if (state.name.isBlank() || state.email.isBlank() ||
            state.phone.isBlank() || state.password.isBlank()
        ) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = registerUseCase(
                    name = state.name.trim(),
                    email = state.email.trim(),
                    phone = state.phone.trim(),
                    password = state.password
                )
                authManager.saveAccessToken(token)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegistered = true,
                        toastMessage = "Account created successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Registration failed"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
