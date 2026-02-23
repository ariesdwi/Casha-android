package com.casha.app.ui.feature.liability.subviews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LiabilityCreditCardSectionsView(
    liabilityState: com.casha.app.ui.feature.liability.LiabilityState,
    liability: Liability,
    userCurrency: String,
    onPaymentClick: (amount: Double, type: PaymentType) -> Unit,
    onStatementClick: (LiabilityStatement) -> Unit,
    onTransactionClick: (LiabilityTransaction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PaymentSection(liabilityState, liability, userCurrency, onPaymentClick)
        LatestStatementSection(liabilityState, onStatementClick)
        UnbilledTransactionsSection(liabilityState, userCurrency, onTransactionClick)
        CreditLimitProgressSection(liability, userCurrency)
    }
}

@Composable
private fun PaymentSection(
    liabilityState: com.casha.app.ui.feature.liability.LiabilityState,
    liability: Liability,
    userCurrency: String,
    onPaymentClick: (Double, PaymentType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jatuh Tempo: ${liabilityState.latestStatement?.dueDate?.let { formatShortDate(it) } ?: "N/A"}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Minimum Payment", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = formatLiabilityCurrency(liabilityState.latestStatement?.minimumPayment ?: 0.0, userCurrency),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onPaymentClick(liabilityState.latestStatement?.minimumPayment ?: 0.0, PaymentType.MINIMUM) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Bayar Minimum", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onPaymentClick(liability.currentBalance, PaymentType.FULL) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Bayar Penuh", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LatestStatementSection(
    liabilityState: com.casha.app.ui.feature.liability.LiabilityState,
    onStatementClick: (LiabilityStatement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Billing Statements", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("${liabilityState.statements.size} statements", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (liabilityState.statements.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    liabilityState.statements.forEachIndexed { index, statement ->
                        StatementListRow(statement, onClick = { onStatementClick(statement) })
                        if (index < liabilityState.statements.size - 1) {
                            Divider(modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        } else if (liabilityState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No statements available",
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatementListRow(statement: LiabilityStatement, onClick: () -> Unit) {
    val statusColor = statusColor(statement.status)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(statusColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
        }

        Text(
            text = "${formatShortDate(statement.startDate)} - ${formatShortDate(statement.endDate)}",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun UnbilledTransactionsSection(
    liabilityState: com.casha.app.ui.feature.liability.LiabilityState,
    userCurrency: String,
    onTransactionClick: (LiabilityTransaction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Transaksi Belum Tertagih", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            liabilityState.unbilledTransactions?.let { unbilled ->
                Text("${unbilled.transactionCount} transaksi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val unbilled = liabilityState.unbilledTransactions
        if (unbilled != null && unbilled.transactions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    unbilled.transactions.forEachIndexed { index, transaction ->
                        TransactionRow(transaction, userCurrency, onClick = { onTransactionClick(transaction) })
                        if (index < unbilled.transactions.size - 1) {
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        } else if (liabilityState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Belum ada transaksi",
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: LiabilityTransaction, userCurrency: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
            Text(text = formatDayStr(transaction.datetime), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = formatMonthStr(transaction.datetime), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = transaction.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = transactionAmountText(transaction, userCurrency),
                    fontWeight = FontWeight.Bold,
                    color = transactionAmountColor(transaction)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = transaction.category?.name ?: "Other", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                val isBilled = transaction.billingStatementId != null
                Box(
                    modifier = Modifier
                        .background(if (isBilled) Color.Blue.copy(alpha = 0.1f) else Color(0xFFFFA500).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isBilled) "Billed" else "Unbilled",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBilled) Color.Blue else Color(0xFFFFA500)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditLimitProgressSection(liability: Liability, userCurrency: String) {
    val limit = liability.creditLimit ?: return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Penggunaan Kredit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                val usagePercent = (liability.currentBalance / limit).coerceIn(0.0, 1.0)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE5E5EA))) {
                        Box(modifier = Modifier.fillMaxWidth(usagePercent.toFloat()).fillMaxHeight().background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = if (usagePercent > 0.8) listOf(Color(0xFFFF9800), Color(0xFFFF3B30)) else listOf(Color(0xFF6750A4).copy(alpha = 0.8f), Color(0xFF6750A4))
                            )
                        ))
                    }
                    Text(text = String.format(Locale.US, "%.1f%%", usagePercent * 100), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    DetailInfoRow(Icons.Default.ArrowUpward, "Terpakai", formatLiabilityCurrency(liability.currentBalance, userCurrency))
                    DetailInfoRow(Icons.Default.ArrowDownward, "Tersisa", formatLiabilityCurrency(limit - liability.currentBalance, userCurrency))
                    DetailInfoRow(Icons.Default.Lock, "Limit", formatLiabilityCurrency(limit, userCurrency))
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(Color.Gray.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        }

        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun statusColor(status: StatementStatus): Color {
    return when (status) {
        StatementStatus.PAID -> Color(0xFF43A047) // Material Green
        StatementStatus.OPEN -> Color(0xFF1E88E5) // Material Blue
        StatementStatus.PARTIAL -> Color(0xFFFF9800) // Muted Orange
        StatementStatus.LATE -> Color(0xFFE53935) // Material Red
    }
}

private fun formatShortDate(date: Date): String {
    return try {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        date.toString()
    }
}

private fun formatMonthStr(date: Date): String {
    return try {
        val formatter = SimpleDateFormat("MMM", Locale.getDefault())
        formatter.format(date).uppercase()
    } catch (e: Exception) {
        ""
    }
}

private fun formatDayStr(date: Date): String {
    return try {
        val formatter = SimpleDateFormat("dd", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        ""
    }
}

private fun transactionAmountText(transaction: LiabilityTransaction, userCurrency: String): String {
    val isCredit = isTransactionCredit(transaction)
    val displayAmount = Math.abs(transaction.amount)
    val prefix = if (isCredit) "+" else "-"
    return "$prefix${formatLiabilityCurrency(displayAmount, userCurrency)}"
}

private fun transactionAmountColor(transaction: LiabilityTransaction): Color {
    return if (isTransactionCredit(transaction)) Color(0xFF43A047) else Color(0xFFE53935)
}

private fun isTransactionCredit(transaction: LiabilityTransaction): Boolean {
    if (transaction.type?.uppercase(Locale.getDefault()) == "CREDIT") {
        return true
    }
    return transaction.amount < 0
}
