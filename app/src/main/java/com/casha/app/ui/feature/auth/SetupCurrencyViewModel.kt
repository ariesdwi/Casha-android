package com.casha.app.ui.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casha.app.core.auth.AuthManager
import com.casha.app.domain.model.UpdateProfileRequest
import com.casha.app.domain.usecase.auth.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)

data class SetupCurrencyUiState(
    val selectedCurrency: CurrencyOption? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val hasSelectedCurrency: Boolean = false
)

@HiltViewModel
class SetupCurrencyViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupCurrencyUiState())
    val uiState: StateFlow<SetupCurrencyUiState> = _uiState.asStateFlow()

    val supportedCurrencies = listOf(
        // ASEAN
        CurrencyOption("IDR", "Rp", "Indonesian Rupiah"),
        CurrencyOption("SGD", "S$", "Singapore Dollar"),
        CurrencyOption("MYR", "RM", "Malaysian Ringgit"),
        CurrencyOption("VND", "₫", "Vietnamese Dong"),
        CurrencyOption("THB", "฿", "Thai Baht"),
        CurrencyOption("PHP", "₱", "Philippine Peso"),
        CurrencyOption("BND", "B$", "Brunei Dollar"),
        CurrencyOption("KHR", "៛", "Cambodian Riel"),
        CurrencyOption("LAK", "₭", "Lao Kip"),
        CurrencyOption("MMK", "Ks", "Myanmar Kyat"),
        CurrencyOption("USD", "$", "US Dollar (East Timor also uses)"),

        // Asia
        CurrencyOption("CNY", "¥", "Chinese Yuan"),
        CurrencyOption("JPY", "¥", "Japanese Yen"),
        CurrencyOption("KRW", "₩", "South Korean Won"),
        CurrencyOption("INR", "₹", "Indian Rupee"),
        CurrencyOption("BDT", "৳", "Bangladeshi Taka"),
        CurrencyOption("PKR", "₨", "Pakistani Rupee"),
        CurrencyOption("LKR", "Rs", "Sri Lankan Rupee"),
        CurrencyOption("NPR", "₨", "Nepalese Rupee"),

        // Middle East
        CurrencyOption("SAR", "﷼", "Saudi Riyal"),
        CurrencyOption("AED", "د.إ", "UAE Dirham"),
        CurrencyOption("QAR", "﷼", "Qatari Riyal"),
        CurrencyOption("KWD", "د.ك", "Kuwaiti Dinar"),
        CurrencyOption("OMR", "﷼", "Omani Rial"),
        CurrencyOption("BHD", "د.ب", "Bahraini Dinar"),

        // Americas
        CurrencyOption("USD", "$", "US Dollar"),
        CurrencyOption("CAD", "C$", "Canadian Dollar"),
        CurrencyOption("BRL", "R$", "Brazilian Real"),
        CurrencyOption("MXN", "$", "Mexican Peso"),
        CurrencyOption("ARS", "$", "Argentine Peso"),

        // Europe
        CurrencyOption("EUR", "€", "Euro"),
        CurrencyOption("GBP", "£", "British Pound"),
        CurrencyOption("CHF", "CHF", "Swiss Franc"),
        CurrencyOption("SEK", "kr", "Swedish Krona"),
        CurrencyOption("NOK", "kr", "Norwegian Krone"),
        CurrencyOption("DKK", "kr", "Danish Krone"),
        CurrencyOption("RUB", "₽", "Russian Ruble"),
        CurrencyOption("TRY", "₺", "Turkish Lira"),

        // Oceania
        CurrencyOption("AUD", "A$", "Australian Dollar"),
        CurrencyOption("NZD", "NZ$", "New Zealand Dollar"),

        // Africa
        CurrencyOption("ZAR", "R", "South African Rand"),
        CurrencyOption("EGP", "£", "Egyptian Pound"),
        CurrencyOption("NGN", "₦", "Nigerian Naira"),
        CurrencyOption("KES", "KSh", "Kenyan Shilling"),
        CurrencyOption("GHS", "₵", "Ghanaian Cedi")
    ).distinctBy { it.code }

    init {
        checkExistingCurrency()
    }

    private fun checkExistingCurrency() {
        viewModelScope.launch {
            authManager.selectedCurrency.collect { currentCurrency ->
                if (!currentCurrency.isNullOrEmpty()) {
                    val option = supportedCurrencies.find { it.code == currentCurrency }
                    _uiState.update { it.copy(
                        selectedCurrency = option,
                        hasSelectedCurrency = true 
                    ) }
                } else {
                    // Default to IDR if possible
                    if (_uiState.value.selectedCurrency == null) {
                        _uiState.update { it.copy(
                            selectedCurrency = supportedCurrencies.find { it.code == "IDR" } ?: supportedCurrencies.firstOrNull()
                        ) }
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCurrencySelected(currency: CurrencyOption) {
        if (_uiState.value.hasSelectedCurrency) return
        _uiState.update { it.copy(selectedCurrency = currency) }
    }

    fun saveCurrency() {
        val selected = _uiState.value.selectedCurrency ?: return
        if (_uiState.value.hasSelectedCurrency) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Update remote profile
                updateProfileUseCase(UpdateProfileRequest(currency = selected.code))
                
                // 2. Save locally
                authManager.saveCurrency(selected.code)
                
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                // Even if remote fails, we might still want to proceed if we saved locally?
                // But iOS code catches and suppresses error.
                authManager.saveCurrency(selected.code)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
