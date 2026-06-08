package com.casha.app.ui.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.casha.app.R
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.*
import com.casha.app.ui.theme.*

/**
 * Compact wallet summary component that displays total balance across all wallets
 * with expand/collapse functionality to show full WalletCardDeck.
 *
 * In collapsed state (~52dp), shows:
 * - Total balance across all wallets
 * - Wallet count
 * - Optional net cashflow for period
 * - Expand/collapse icon
 *
 * In expanded state, shows full WalletCardDeck inline with smooth animation.
 */
@Composable
fun WalletSummaryCompact(
    wallets: List<Wallet>,
    summary: WalletSummary?,
    cashflowSummary: CashflowSummary?,
    defaultWalletId: String?,
    isLoading: Boolean,
    selectedPeriod: SpendingPeriod,
    onPeriodChange: (SpendingPeriod) -> Unit,
    onManageWallets: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isBalanceVisible by remember { mutableStateOf(true) }

    // Calculate total balance (liquid balance minus credit used = net worth)
    val totalBalance = if (summary != null) {
        summary.liquidBalance - summary.totalCreditUsed
    } else {
        wallets.sumOf { it.balance }
    }
    val walletCount = wallets.size
    val netCashflow = cashflowSummary?.netBalance ?: 0.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Collapsed state - always visible
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
                .clickable(
                    onClickLabel = if (isExpanded) "Collapse wallet details" else "Expand wallet details"
                ) { isExpanded = !isExpanded },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First row: Icon + Total Assets label + Amount + Expand icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Wallet icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.dashboard_wallet_summary_total),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isBalanceVisible) {
                                    CurrencyFormatter.format(totalBalance)
                                } else {
                                    "••••••••"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.semantics {
                                    contentDescription = if (isBalanceVisible) {
                                        "Total assets: ${CurrencyFormatter.format(totalBalance)}"
                                    } else {
                                        "Total assets hidden"
                                    }
                                }
                            )
                        }
                    }

                    // Expand/Collapse icon
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(40.dp)
                            .semantics {
                                contentDescription = if (isExpanded) {
                                    "Collapse wallet details"
                                } else {
                                    "Expand wallet details"
                                }
                            }
                    ) {
                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Second row: Wallet count • Net cashflow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_wallet_summary_wallets, walletCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            contentDescription = "$walletCount wallets"
                        }
                    )

                    if (cashflowSummary != null) {
                        Text(
                            text = " • Net: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )

                        val netColor = when {
                            netCashflow > 0 -> CashaSuccess
                            netCashflow < 0 -> CashaDanger
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Text(
                            text = if (isBalanceVisible) {
                                CurrencyFormatter.format(netCashflow)
                            } else {
                                "••••"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = netColor,
                            modifier = Modifier.semantics {
                                contentDescription = if (isBalanceVisible) {
                                    "Net cashflow: ${CurrencyFormatter.format(netCashflow)}"
                                } else {
                                    "Net cashflow hidden"
                                }
                            }
                        )
                    }
                }
            }
        }


        // Expanded state - full WalletCardDeck with animation
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Spacer(modifier = Modifier.height(4.dp))
                
                WalletCardDeck(
                    wallets = wallets,
                    summary = summary,
                    cashflowSummary = cashflowSummary,
                    defaultWalletId = defaultWalletId,
                    isLoading = isLoading,
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = onPeriodChange,
                    onManageWallets = onManageWallets
                )
            }
        }
    }
}

/**
 * Loading shimmer state for WalletSummaryCompact
 */
@Composable
fun WalletSummaryCompactShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon shimmer
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Label shimmer
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        )
                        // Amount shimmer
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        )
                    }
                }
            }

            // Second row shimmer
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
        }
    }
}
