package com.casha.app.ui.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.auth.AuthManager
import com.casha.app.domain.model.UpdateProfileRequest
import com.casha.app.domain.usecase.auth.UpdateProfileUseCase
import kotlinx.coroutines.launch

data class CurrencyOption(
    val code: String,
    val name: String,
    val flag: String
)

private val currencies = listOf(
    CurrencyOption("USD", "US Dollar", "🇺🇸"),
    CurrencyOption("IDR", "Indonesian Rupiah", "🇮🇩"),
    CurrencyOption("SGD", "Singapore Dollar", "🇸🇬"),
    CurrencyOption("EUR", "Euro", "🇪🇺"),
    CurrencyOption("GBP", "British Pound", "🇬🇧"),
    CurrencyOption("JPY", "Japanese Yen", "🇯🇵"),
    CurrencyOption("KRW", "South Korean Won", "🇰🇷"),
    CurrencyOption("CNY", "Chinese Yuan", "🇨🇳"),
    CurrencyOption("INR", "Indian Rupee", "🇮🇳"),
    CurrencyOption("MYR", "Malaysian Ringgit", "🇲🇾"),
    CurrencyOption("THB", "Thai Baht", "🇹🇭"),
    CurrencyOption("AUD", "Australian Dollar", "🇦🇺"),
    CurrencyOption("CAD", "Canadian Dollar", "🇨🇦"),
    CurrencyOption("CHF", "Swiss Franc", "🇨🇭"),
    CurrencyOption("SAR", "Saudi Riyal", "🇸🇦"),
    CurrencyOption("AED", "UAE Dirham", "🇦🇪"),
    CurrencyOption("BRL", "Brazilian Real", "🇧🇷"),
    CurrencyOption("PHP", "Philippine Peso", "🇵🇭"),
    CurrencyOption("VND", "Vietnamese Dong", "🇻🇳"),
    CurrencyOption("HKD", "Hong Kong Dollar", "🇭🇰")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupCurrencyScreen(
    authManager: AuthManager,
    updateProfileUseCase: UpdateProfileUseCase,
    onCurrencySet: () -> Unit
) {
    var selectedCurrency by remember { mutableStateOf<CurrencyOption?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isBlank()) currencies
        else currencies.filter {
            it.code.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "💱",
                fontSize = 48.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Choose Your Currency",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select the currency you use most",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search currency...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Currency List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCurrencies) { currency ->
                    CurrencyItem(
                        currency = currency,
                        isSelected = currency == selectedCurrency,
                        onClick = { selectedCurrency = currency }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue Button
            Button(
                onClick = {
                    selectedCurrency?.let { currency ->
                        scope.launch {
                            isLoading = true
                            try {
                                authManager.saveCurrency(currency.code)
                                updateProfileUseCase(
                                    UpdateProfileRequest(currency = currency.code)
                                )
                            } catch (_: Exception) {
                                // Currency saved locally even if API call fails
                            }
                            isLoading = false
                            onCurrencySet()
                        }
                    }
                },
                enabled = selectedCurrency != null && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CurrencyItem(
    currency: CurrencyOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = currency.flag, fontSize = 28.sp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
