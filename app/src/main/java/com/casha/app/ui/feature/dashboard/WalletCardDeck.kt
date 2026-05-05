package com.casha.app.ui.feature.dashboard

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.*
import com.casha.app.ui.feature.wallet.WalletCardDeckShimmer
import com.casha.app.ui.theme.*

@Composable
fun WalletCardDeck(
    wallets: List<Wallet>,
    summary: WalletSummary?,
    cashflowSummary: CashflowSummary?,
    defaultWalletId: String?,
    isLoading: Boolean,
    selectedPeriod: SpendingPeriod,
    onPeriodChange: (SpendingPeriod) -> Unit,
    onManageWallets: () -> Unit
) {
    // Show shimmer when still loading and no data available yet
    if (isLoading && wallets.isEmpty() && summary == null) {
        WalletCardDeckShimmer()
        return
    }

    var isBalanceVisible by remember { mutableStateOf(true) }
    val pageCount = 1 + wallets.size // Index 0 = Net Cashflow, 1..n = wallets
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLoading) 0.85f else 1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            pageSpacing = 12.dp
        ) { page ->
            if (page == 0) {
                NetCashflowCard(
                    summary = cashflowSummary,
                    isBalanceVisible = isBalanceVisible,
                    onToggleVisibility = { isBalanceVisible = !isBalanceVisible },
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = onPeriodChange
                )
            } else {
                val wallet = wallets[page - 1]
                WalletCard(
                    wallet = wallet,
                    isDefault = wallet.id == defaultWalletId,
                    isBalanceVisible = isBalanceVisible,
                    onToggleVisibility = { isBalanceVisible = !isBalanceVisible }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(if (isSelected) 20.dp else 8.dp, 8.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}

@Composable
private fun NetCashflowCard(
    summary: CashflowSummary?,
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    selectedPeriod: SpendingPeriod,
    onPeriodChange: (SpendingPeriod) -> Unit
) {
    val netBalance = summary?.netBalance ?: 0.0
    val amountColor = when {
        netBalance > 0 -> CashaSuccess
        netBalance < 0 -> CashaDanger
        else -> MaterialTheme.colorScheme.onSurface
    }
    var showPeriodMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Net Cashflow",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isBalanceVisible) CurrencyFormatter.format(netBalance) else "••••••••",
                    style = MaterialTheme.typography.headlineMedium,
                    color = amountColor,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(20.dp)) {
                    Icon(
                        if (isBalanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Period selector
            Box {
                Row(
                    modifier = Modifier.clickable { showPeriodMenu = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val periodLabel = when (selectedPeriod) {
                        SpendingPeriod.THIS_WEEK -> "This Week"
                        SpendingPeriod.THIS_MONTH -> "This Month"
                        SpendingPeriod.LAST_MONTH -> "Last Month"
                        SpendingPeriod.LAST_THREE_MONTHS -> "Last 3 Months"
                        SpendingPeriod.THIS_YEAR -> "This Year"
                        SpendingPeriod.ALL_TIME -> "All Time"
                        SpendingPeriod.FUTURE -> "Future"
                        is SpendingPeriod.CUSTOM -> "Custom"
                    }
                    Text(
                        periodLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(expanded = showPeriodMenu, onDismissRequest = { showPeriodMenu = false }) {
                    listOf(
                        SpendingPeriod.THIS_WEEK,
                        SpendingPeriod.THIS_MONTH,
                        SpendingPeriod.LAST_MONTH,
                        SpendingPeriod.LAST_THREE_MONTHS,
                        SpendingPeriod.THIS_YEAR,
                        SpendingPeriod.ALL_TIME
                    ).forEach { period ->
                        val label = when (period) {
                            SpendingPeriod.THIS_WEEK -> "This Week"
                            SpendingPeriod.THIS_MONTH -> "This Month"
                            SpendingPeriod.LAST_MONTH -> "Last Month"
                            SpendingPeriod.LAST_THREE_MONTHS -> "Last 3 Months"
                            SpendingPeriod.THIS_YEAR -> "This Year"
                            SpendingPeriod.ALL_TIME -> "All Time"
                            SpendingPeriod.FUTURE -> "Future"
                            is SpendingPeriod.CUSTOM -> "Custom"
                        }
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { onPeriodChange(period); showPeriodMenu = false }
                        )
                    }
                }
            }

            // In / Out row with divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CallMade, contentDescription = null, tint = CashaSuccess, modifier = Modifier.size(14.dp))
                    Column {
                        Text("In", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (isBalanceVisible) CurrencyFormatter.format(summary?.totalIncome ?: 0.0) else "••••",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CallReceived, contentDescription = null, tint = CashaDanger, modifier = Modifier.size(14.dp))
                    Column {
                        Text("Out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (isBalanceVisible) CurrencyFormatter.format(summary?.totalExpense ?: 0.0) else "••••",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletCard(
    wallet: Wallet,
    isDefault: Boolean,
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    val cardColor = walletCardColor(wallet.type)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: icon + name + visibility toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(cardColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(walletTypeIconFor(wallet.type), contentDescription = null, tint = cardColor, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(wallet.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    if (isDefault) {
                        Spacer(Modifier.width(4.dp))
                        Text("★", color = Color(0xFFFFB300), fontSize = 12.sp)
                    }
                }
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(20.dp)) {
                    Icon(
                        if (isBalanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Balance
            Column {
                Text(
                    if (wallet.source == WalletSource.LOAN) "Used" else "Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    if (isBalanceVisible) CurrencyFormatter.format(wallet.balance) else "••••••••",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bottom row: bank + available credit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    wallet.bankName ?: wallet.type.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (wallet.source == WalletSource.LOAN && wallet.availableCredit != null) {
                    Text(
                        "Avail: ${if (isBalanceVisible) CurrencyFormatter.format(wallet.availableCredit!!) else "••••"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CashaSuccess
                    )
                }
            }
        }
    }
}

private fun walletTypeIconFor(type: WalletType) = when (type) {
    WalletType.CASH -> Icons.Default.Money
    WalletType.SAVINGS_ACCOUNT -> Icons.Default.AccountBalance
    WalletType.CHECKING_ACCOUNT -> Icons.Default.AccountBalance
    WalletType.E_WALLET -> Icons.Default.PhoneAndroid
    WalletType.CREDIT_CARD -> Icons.Default.CreditCard
    WalletType.OTHER -> Icons.Default.AccountBalanceWallet
}

private fun walletCardColor(type: WalletType) = when (type) {
    WalletType.CASH -> Color(0xFF4CAF50)
    WalletType.SAVINGS_ACCOUNT -> Color(0xFF2196F3)
    WalletType.CHECKING_ACCOUNT -> Color(0xFF673AB7)
    WalletType.E_WALLET -> Color(0xFFFF9800)
    WalletType.CREDIT_CARD -> Color(0xFFE91E63)
    WalletType.OTHER -> Color(0xFF607D8B)
}
