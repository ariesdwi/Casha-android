package com.casha.app.ui.feature.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.AddCreditCardRequest
import com.casha.app.domain.model.AddLiquidWalletRequest
import com.casha.app.domain.model.WalletType
import com.casha.app.ui.component.CurrencyInputField

private enum class WalletTab { LIQUID, CREDIT_CARD }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(WalletTab.LIQUID) }

    // Liquid wallet fields
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WalletType.SAVINGS_ACCOUNT) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var amountValue by remember { mutableStateOf(0.0) }
    var bankName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Credit card fields
    var ccName by remember { mutableStateOf("") }
    var ccBankName by remember { mutableStateOf("") }
    var ccLimitText by remember { mutableStateOf("") }
    var ccLimitValue by remember { mutableStateOf(0.0) }
    var ccBalanceText by remember { mutableStateOf("") }
    var ccBalanceValue by remember { mutableStateOf(0.0) }
    var ccBillingDay by remember { mutableStateOf("") }
    var ccDueDay by remember { mutableStateOf("") }
    var ccInterestRateText by remember { mutableStateOf("") }
    var ccInterestRateValue by remember { mutableStateOf(0.0) }
    var ccInterestType by remember { mutableStateOf("MONTHLY") }
    var showInterestTypeDropdown by remember { mutableStateOf(false) }
    var ccMinPaymentText by remember { mutableStateOf("") }
    var ccMinPaymentValue by remember { mutableStateOf(0.0) }
    var ccLateFeeText by remember { mutableStateOf("") }
    var ccLateFeeValue by remember { mutableStateOf(0.0) }

    val isLiquidValid = name.isNotBlank() && amountValue >= 0
    val isCreditValid = ccName.isNotBlank() && ccBankName.isNotBlank() && ccLimitValue > 0 && ccInterestRateValue >= 0
    val isFormValid = if (selectedTab == WalletTab.LIQUID) isLiquidValid else isCreditValid

    val liquidTypes = listOf(
        WalletType.CASH, WalletType.SAVINGS_ACCOUNT,
        WalletType.CHECKING_ACCOUNT, WalletType.E_WALLET, WalletType.OTHER
    )

    fun walletTypeLabel(type: WalletType) =
        type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

    fun walletTypeIcon(type: WalletType) = when (type) {
        WalletType.CASH -> Icons.Default.Money
        WalletType.SAVINGS_ACCOUNT -> Icons.Default.Savings
        WalletType.CHECKING_ACCOUNT -> Icons.Default.AccountBalance
        WalletType.E_WALLET -> Icons.Default.PhoneAndroid
        else -> Icons.Default.AccountBalanceWallet
    }

    fun save() {
        if (selectedTab == WalletTab.LIQUID) {
            viewModel.addLiquidWallet(
                AddLiquidWalletRequest(
                    name = name.trim(),
                    type = selectedType,
                    amount = amountValue,
                    description = description.takeIf { it.isNotBlank() },
                    bankName = bankName.takeIf { it.isNotBlank() }
                )
            )
        } else {
            viewModel.addCreditCard(
                AddCreditCardRequest(
                    name = ccName.trim(),
                    bankName = ccBankName.trim(),
                    creditLimit = ccLimitValue,
                    currentBalance = ccBalanceValue,
                    interestRate = ccInterestRateValue,
                    billingDay = ccBillingDay.toIntOrNull(),
                    dueDay = ccDueDay.toIntOrNull(),
                    interestType = ccInterestType,
                    minPaymentPercentage = ccMinPaymentValue.takeIf { it > 0 },
                    lateFee = ccLateFeeValue.takeIf { it > 0 }
                )
            )
        }
        onNavigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    "Add Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = { save() },
                    enabled = isFormValid && !uiState.isLoading
                ) {
                    Text(
                        "Save",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isFormValid && !uiState.isLoading)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            // ── Scrollable Body ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(20.dp))

                // Tab Switcher
                AddWalletTabSwitcher(selectedTab = selectedTab, onSelect = { selectedTab = it })

                Spacer(Modifier.height(24.dp))

                // ── Cash / Bank Form ──────────────────────────────────────
                AnimatedVisibility(visible = selectedTab == WalletTab.LIQUID) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        AddWalletSection("Account Details") {
                            AddWalletField("Account Name", required = true) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = { Text("e.g. BCA Tabungan") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = cashaFieldColors()
                                )
                            }

                            AddWalletField("Account Type", required = true) {
                                Box {
                                    OutlinedButton(
                                        onClick = { showTypeDropdown = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Icon(
                                            walletTypeIcon(selectedType),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            walletTypeLabel(selectedType),
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showTypeDropdown,
                                        onDismissRequest = { showTypeDropdown = false }
                                    ) {
                                        liquidTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(walletTypeLabel(type)) },
                                                leadingIcon = {
                                                    Icon(
                                                        walletTypeIcon(type),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                },
                                                onClick = {
                                                    selectedType = type
                                                    showTypeDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            AddWalletField("Bank Name") {
                                OutlinedTextField(
                                    value = bankName,
                                    onValueChange = { bankName = it },
                                    placeholder = { Text("e.g. BCA") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = cashaFieldColors()
                                )
                            }
                        }

                        AddWalletSection("Balance") {
                            AddWalletField("Initial Balance", required = true) {
                                CurrencyInputField(
                                    value = amountText,
                                    onValueChange = {
                                        amountText = it
                                        amountValue = it.toDoubleOrNull() ?: 0.0
                                    },
                                    placeholder = { Text("0") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = {
                                        Text(
                                            CurrencyFormatter.symbol(CurrencyFormatter.defaultCurrency),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }

                        AddWalletSection("Notes") {
                            AddWalletField("Description") {
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    placeholder = { Text("Optional note about this account") },
                                    singleLine = false,
                                    minLines = 2,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = cashaFieldColors()
                                )
                            }
                        }
                    }
                }

                // ── Credit Card Form ──────────────────────────────────────
                AnimatedVisibility(visible = selectedTab == WalletTab.CREDIT_CARD) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        AddWalletSection("Card Details") {
                            AddWalletField("Card Name", required = true) {
                                OutlinedTextField(
                                    value = ccName,
                                    onValueChange = { ccName = it },
                                    placeholder = { Text("e.g. Citi Platinum") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = cashaFieldColors()
                                )
                            }
                            AddWalletField("Bank / Issuer", required = true) {
                                OutlinedTextField(
                                    value = ccBankName,
                                    onValueChange = { ccBankName = it },
                                    placeholder = { Text("e.g. Citibank") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = cashaFieldColors()
                                )
                            }
                        }

                        AddWalletSection("Credit Limit & Balance") {
                            AddWalletField("Credit Limit", required = true) {
                                CurrencyInputField(
                                    value = ccLimitText,
                                    onValueChange = {
                                        ccLimitText = it
                                        ccLimitValue = it.toDoubleOrNull() ?: 0.0
                                    },
                                    placeholder = { Text("0") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = {
                                        Text(
                                            CurrencyFormatter.symbol(CurrencyFormatter.defaultCurrency),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                            AddWalletField("Current Balance Used") {
                                CurrencyInputField(
                                    value = ccBalanceText,
                                    onValueChange = {
                                        ccBalanceText = it
                                        ccBalanceValue = it.toDoubleOrNull() ?: 0.0
                                    },
                                    placeholder = { Text("0") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = {
                                        Text(
                                            CurrencyFormatter.symbol(CurrencyFormatter.defaultCurrency),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }

                        AddWalletSection("Billing Cycle") {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AddWalletField("Billing Day", modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = ccBillingDay,
                                        onValueChange = {
                                            if (it.length <= 2) ccBillingDay = it.filter { c -> c.isDigit() }
                                        },
                                        placeholder = { Text("1–31") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = cashaFieldColors()
                                    )
                                }
                                AddWalletField("Due Day", modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = ccDueDay,
                                        onValueChange = {
                                            if (it.length <= 2) ccDueDay = it.filter { c -> c.isDigit() }
                                        },
                                        placeholder = { Text("1–31") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = cashaFieldColors()
                                    )
                                }
                            }
                        }

                        AddWalletSection("Interest") {
                            AddWalletField("Interest Rate (%)", required = true) {
                                OutlinedTextField(
                                    value = ccInterestRateText,
                                    onValueChange = {
                                        ccInterestRateText = it
                                        ccInterestRateValue = it.toDoubleOrNull() ?: 0.0
                                    },
                                    placeholder = { Text("e.g. 2.25") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = cashaFieldColors()
                                )
                            }
                            AddWalletField("Interest Type") {
                                Box {
                                    OutlinedButton(
                                        onClick = { showInterestTypeDropdown = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            ccInterestType,
                                            modifier = Modifier.weight(1f),
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showInterestTypeDropdown,
                                        onDismissRequest = { showInterestTypeDropdown = false }
                                    ) {
                                        listOf("MONTHLY", "FLAT").forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type) },
                                                onClick = {
                                                    ccInterestType = type
                                                    showInterestTypeDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AddWalletSection("Optional Fees") {
                            AddWalletField("Min Payment %") {
                                OutlinedTextField(
                                    value = ccMinPaymentText,
                                    onValueChange = {
                                        ccMinPaymentText = it
                                        ccMinPaymentValue = it.toDoubleOrNull() ?: 0.0
                                    },
                                    placeholder = { Text("e.g. 5.0") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = cashaFieldColors()
                                )
                            }
                            AddWalletField("Late Fee") {
                                CurrencyInputField(
                                    value = ccLateFeeText,
                                    onValueChange = {
                                        ccLateFeeText = it
                                        ccLateFeeValue = it.toDoubleOrNull() ?: 0.0
                                    },
                                    placeholder = { Text("0") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    leadingIcon = {
                                        Text(
                                            CurrencyFormatter.symbol(CurrencyFormatter.defaultCurrency),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Save Button
                Button(
                    onClick = { save() },
                    enabled = isFormValid && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Wallet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    } // end Scaffold

    // ── Loading Overlay ──────────────────────────────────────────────────
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        "Saving wallet…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
    } // end outer Box
}

// ── Segmented Tab Switcher ─────────────────────────────────────────────────────
@Composable
private fun AddWalletTabSwitcher(selectedTab: WalletTab, onSelect: (WalletTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        WalletTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            val bgColor by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "tab_bg"
            )
            val contentColor by animateColorAsState(
                if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "tab_txt"
            )
            Surface(
                onClick = { onSelect(tab) },
                shape = RoundedCornerShape(10.dp),
                color = bgColor,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (tab == WalletTab.LIQUID) Icons.Default.AccountBalance else Icons.Default.CreditCard,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = contentColor
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (tab == WalletTab.LIQUID) "Cash / Bank" else "Credit Card",
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = contentColor
                    )
                }
            }
        }
    }
}

// ── Form Section Card ──────────────────────────────────────────────────────────
@Composable
private fun AddWalletSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

// ── Field Label + Content ──────────────────────────────────────────────────────
@Composable
private fun AddWalletField(
    label: String,
    required: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (required) {
                Text(
                    " *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        content()
    }
}

// ── Themed outlined field colors ───────────────────────────────────────────────
@Composable
private fun cashaFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary
)
