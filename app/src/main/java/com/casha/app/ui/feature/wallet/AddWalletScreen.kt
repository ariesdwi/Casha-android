package com.casha.app.ui.feature.wallet

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

enum class WalletTab { LIQUID, CREDIT_CARD }

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Step 1 — Type Picker Sheet
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

data class WalletTypeItem(
    val tab: WalletTab,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

private val walletTypeItems = listOf(
    WalletTypeItem(
        tab = WalletTab.LIQUID,
        title = "Cash / Bank Account",
        description = "Savings, checking, e-wallet, cash",
        icon = Icons.Default.AccountBalance,
        color = Color(0xFF2196F3)
    ),
    WalletTypeItem(
        tab = WalletTab.CREDIT_CARD,
        title = "Credit Card",
        description = "Credit card with limit & billing cycle",
        icon = Icons.Default.CreditCard,
        color = Color(0xFF9C27B0)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectWalletTypeSheet(
    onDismiss: () -> Unit,
    onTypeSelected: (WalletTab) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = Modifier.fillMaxSize(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    "Add Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose the type of wallet to add",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))

            // Type grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(walletTypeItems) { item ->
                    WalletTypeCard(item = item, onClick = { onTypeSelected(item.tab) })
                }
            }
        }
    }
}

@Composable
private fun WalletTypeCard(item: WalletTypeItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Step 2 — Add Wallet Form Sheet
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletSheet(
    initialTab: WalletTab,
    viewModel: WalletViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Navigate back on success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "Wallet added" || uiState.successMessage == "Credit card added") {
            viewModel.clearMessages()
            onDismiss()
        }
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxSize(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        AddWalletFormContent(
            initialTab = initialTab,
            viewModel = viewModel,
            uiState = uiState
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Form Content (shared)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
private fun AddWalletFormContent(
    initialTab: WalletTab,
    viewModel: WalletViewModel,
    uiState: WalletUiState
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

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
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // ── Sheet Header ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    if (selectedTab == WalletTab.LIQUID) "Cash / Bank Account" else "Credit Card",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Fill in the details below",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            // ── Error ─────────────────────────────────────────────────────
            uiState.errorMessage?.let { err ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        err,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // ── Form Body ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Tab switcher (still visible to let user switch)
                AddWalletTabSwitcher(selectedTab = selectedTab, onSelect = { selectedTab = it })

                Spacer(Modifier.height(4.dp))

                if (selectedTab == WalletTab.LIQUID) {
                    // ── Cash / Bank Form ──────────────────────────────────

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
                } else {
                    // ── Credit Card Form ──────────────────────────────────

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

                Spacer(Modifier.height(8.dp))
            }

            // ── Submit Button ─────────────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { save() },
                    enabled = isFormValid && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Text("Save Wallet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ── Segmented Tab Switcher ─────────────────────────────────────────────────────
@Composable
internal fun AddWalletTabSwitcher(selectedTab: WalletTab, onSelect: (WalletTab) -> Unit) {
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
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, label = "tab_bg"
            )
            val contentColor by animateColorAsState(
                if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, label = "tab_txt"
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
                        if (tab == WalletTab.LIQUID) "Cash / Bank" else "Credit Card",
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
internal fun AddWalletSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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

// ── Field wrapper ──────────────────────────────────────────────────────────────
@Composable
internal fun AddWalletField(
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

// ── Themed field colors ────────────────────────────────────────────────────────
@Composable
internal fun cashaFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary
)
