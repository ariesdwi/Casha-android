package com.casha.app.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.auth.AuthManager
import com.casha.app.core.network.NetworkMonitor
import com.casha.app.domain.model.UpdateProfileRequest
import com.casha.app.domain.model.UserCasha
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase
import com.casha.app.domain.usecase.profile.DeleteAccountUseCase
import com.casha.app.domain.usecase.profile.GetProfileUseCase
import com.casha.app.domain.usecase.profile.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserCasha? = null,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val lastUpdated: Date? = null,
    val errorMessage: String? = null,
    val isOnline: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val deleteAllLocalDataUseCase: DeleteAllLocalDataUseCase,
    private val authManager: AuthManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var lastRefreshDate: Long = 0

    init {
        // Monitor network status
        networkMonitor.isOnline
            .onEach { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
                if (isOnline && _uiState.value.profile == null) {
                    refreshProfile()
                }
            }
            .launchIn(viewModelScope)

        // Initial load
        viewModelScope.launch {
            loadCachedProfile()
            refreshProfile()
        }
    }

    private suspend fun loadCachedProfile() {
        val name = authManager.userName.firstOrNull()
        val email = authManager.userEmail.firstOrNull() ?: ""
        val avatar = authManager.userAvatar.firstOrNull()
        val currency = authManager.selectedCurrency.firstOrNull() ?: "USD"

        if (name != null) {
            _uiState.update { 
                it.copy(
                    profile = UserCasha(
                        id = "", // ID doesn't matter much for display
                        email = email,
                        name = name,
                        avatar = avatar,
                        phone = null,
                        currency = currency,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                )
            }
        }
    }

    fun refreshProfile(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastRefreshDate < 5000) return
        lastRefreshDate = now

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            if (_uiState.value.isOnline) {
                try {
                    val profile = getProfileUseCase()
                    _uiState.update { 
                        it.copy(
                            profile = profile, 
                            isLoading = false,
                            lastUpdated = Date()
                        ) 
                    }
                    
                    // Cache profile info
                    authManager.saveProfileInfo(profile.name, profile.email, profile.avatar)
                    authManager.saveCurrency(profile.currency)
                    
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Using cached profile. Failed to update: ${e.message}"
                        ) 
                    }
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Viewing cached profile (offline)"
                    ) 
                }
            }
        }
    }

    fun updateProfile(request: UpdateProfileRequest) {
        if (!_uiState.value.isOnline) {
            _uiState.update { it.copy(errorMessage = "Profile updates require an internet connection") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val updatedProfile = updateProfileUseCase(request)
                _uiState.update { 
                    it.copy(
                        profile = updatedProfile, 
                        isLoading = false,
                        lastUpdated = Date()
                    ) 
                }
                
                // Cache updated info
                authManager.saveProfileInfo(updatedProfile.name, updatedProfile.email, updatedProfile.avatar)
                authManager.saveCurrency(updatedProfile.currency)
                
                // Refresh full profile to be safe
                refreshProfile(force = true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateCurrency(currency: String) {
        val currentProfile = _uiState.value.profile ?: return
        val request = UpdateProfileRequest(
            name = currentProfile.name,
            email = currentProfile.email,
            phone = currentProfile.phone,
            avatar = currentProfile.avatar,
            currency = currency
        )
        updateProfile(request)
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                deleteAccountUseCase()
                logout() // Wipes all local data
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                deleteAllLocalDataUseCase()
                authManager.clearAll()
                _uiState.update { it.copy(isLoading = false, isLoggedOut = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Logout failed")
                }
            }
        }
    }
}
