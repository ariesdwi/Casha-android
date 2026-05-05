package com.casha.app.ui.feature.wallet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Wallet
import com.casha.app.domain.model.WalletSource
import com.casha.app.domain.model.WalletType
import com.casha.app.ui.theme.CashaBlue
import com.casha.app.ui.theme.CashaDanger
import com.casha.app.ui.theme.CashaSuccess
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletListScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddWallet: () -> Unit,
    onNavigateToEditWallet: (String, String) -> Unit,
    onNavigateToTransfer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<Wallet?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    // Show snackbar on messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.loadAll()
            isRefreshing = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // ── Toolbar ──────────────────────────────────────────
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
                    "Wallets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row {
                    IconButton(onClick = onNavigateToTransfer) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Transfer",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = onNavigateToAddWallet) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Add Wallet",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { isRefreshing = true },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.isLoading && uiState.wallets.isEmpty()) {
                        WalletListShimmer()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Summary Card
                            uiState.summary?.let { summary ->
                                item { WalletSummaryCard(summary = summary) }
                            }

                            // Cash & Bank Accounts section
                            if (uiState.liquidWallets.isNotEmpty()) {
                                item { SectionHeader("Cash & Bank Accounts") }
                                items(uiState.liquidWallets, key = { it.id }) { wallet ->
                                    SwipeableWalletRow(
                                        wallet = wallet,
                                        isDefault = wallet.id == uiState.defaultWalletId,
                                        onTap = {
                                            if (wallet.id == uiState.defaultWalletId) viewModel.clearDefault()
                                            else viewModel.setDefault(wallet.id)
                                        },
                                        onEdit = { onNavigateToEditWallet(wallet.id, wallet.source.name) },
                                        onDelete = { showDeleteDialog = wallet }
                                    )
                                }
                            }

                            // Credit Cards section
                            if (uiState.creditWallets.isNotEmpty()) {
                                item { SectionHeader("Credit Cards") }
                                items(uiState.creditWallets, key = { it.id }) { wallet ->
                                    SwipeableWalletRow(
                                        wallet = wallet,
                                        isDefault = wallet.id == uiState.defaultWalletId,
                                        onTap = {
                                            if (wallet.id == uiState.defaultWalletId) viewModel.clearDefault()
                                            else viewModel.setDefault(wallet.id)
                                        },
                                        onEdit = { onNavigateToEditWallet(wallet.id, wallet.source.name) },
                                        onDelete = { showDeleteDialog = wallet }
                                    )
                                }
                            }

                            // Empty state
                            if (uiState.wallets.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                "No wallets yet",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            TextButton(onClick = onNavigateToAddWallet) {
                                                Text("Add your first wallet")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { wallet ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Wallet", fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to delete \"${wallet.name}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWallet(wallet.id, wallet.source)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CashaDanger)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WalletSummaryCard(summary: com.casha.app.domain.model.WalletSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryStatItem(
                label = "Total Cash",
                value = CurrencyFormatter.format(summary.liquidBalance),
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .align(Alignment.CenterVertically)
            )
            SummaryStatItem(
                label = "Available Credit",
                value = CurrencyFormatter.format(summary.availableCredit),
                valueColor = CashaSuccess
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .align(Alignment.CenterVertically)
            )
            SummaryStatItem(
                label = "CC Used",
                value = CurrencyFormatter.format(summary.totalCreditUsed),
                valueColor = if (summary.totalCreditUsed > 0) CashaDanger else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SummaryStatItem(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SwipeableWalletRow(
    wallet: Wallet,
    isDefault: Boolean,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val swipeThreshold = 150f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clipToBounds()
    ) {
        // Background actions (revealed on swipe)
        Row(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CashaDanger,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
        }

        // Foreground card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value < -swipeThreshold) {
                                    offsetX.animateTo(-swipeThreshold, spring(stiffness = Spring.StiffnessLow))
                                } else {
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                val newOffset = (offsetX.value + dragAmount).coerceIn(-swipeThreshold * 1.2f, 0f)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                }
                .clickable { onTap() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored icon badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(walletTypeColor(wallet.type).copy(alpha = 0.10f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = walletTypeIcon(wallet.type),
                        contentDescription = null,
                        tint = walletTypeColor(wallet.type),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                // Name + subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            wallet.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isDefault) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFB300).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "Default",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFB8860B),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        wallet.bankName ?: wallet.type.name.replace("_", " ")
                            .lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Balance column
                Column(horizontalAlignment = Alignment.End) {
                    if (wallet.source == WalletSource.LOAN) {
                        Text(
                            CurrencyFormatter.format(wallet.balance),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = CashaDanger
                        )
                        wallet.availableCredit?.let {
                            Text(
                                "Avail ${CurrencyFormatter.format(it)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CashaSuccess
                            )
                        }
                    } else {
                        Text(
                            CurrencyFormatter.format(wallet.balance),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun walletTypeIcon(type: WalletType) = when (type) {
    WalletType.CASH -> Icons.Default.Money
    WalletType.SAVINGS_ACCOUNT -> Icons.Default.AccountBalance
    WalletType.CHECKING_ACCOUNT -> Icons.Default.AccountBalance
    WalletType.E_WALLET -> Icons.Default.PhoneAndroid
    WalletType.CREDIT_CARD -> Icons.Default.CreditCard
    WalletType.OTHER -> Icons.Default.AccountBalanceWallet
}

private fun walletTypeColor(type: WalletType) = when (type) {
    WalletType.CASH -> Color(0xFF4CAF50)
    WalletType.SAVINGS_ACCOUNT -> Color(0xFF2196F3)
    WalletType.CHECKING_ACCOUNT -> Color(0xFF673AB7)
    WalletType.E_WALLET -> Color(0xFFFF9800)
    WalletType.CREDIT_CARD -> Color(0xFFE91E63)
    WalletType.OTHER -> Color(0xFF607D8B)
}
