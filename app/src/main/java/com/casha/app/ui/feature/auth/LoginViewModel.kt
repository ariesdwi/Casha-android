package com.casha.app.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.auth.AuthManager
import com.casha.app.domain.usecase.auth.LoginUseCase
import com.casha.app.domain.usecase.auth.GoogleLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val needsCurrencySetup: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = loginUseCase(state.email.trim(), state.password)
                authManager.saveAccessToken(result.token)
                if (result.currency != null) {
                    authManager.saveCurrency(result.currency)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, needsCurrencySetup = true) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Login failed"
                    )
                }
            }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = googleLoginUseCase(idToken)
                authManager.saveAccessToken(result.token)
                if (result.currency != null) {
                    authManager.saveCurrency(result.currency)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, needsCurrencySetup = true) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Google login failed"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
