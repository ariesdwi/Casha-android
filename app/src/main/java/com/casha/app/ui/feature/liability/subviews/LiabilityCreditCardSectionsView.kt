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
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

import com.casha.app.ui.feature.liability.subviews.detail.ActiveInstallmentsListView

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
        // Payment section (could be moved to a bottom sheet later)
        PaymentSection(liabilityState, liability, userCurrency, onPaymentClick)
        
        // Active Installments
        ActiveInstallmentsListView(
            installments = liability.installmentPlans ?: emptyList(),
            userCurrency = userCurrency
        )

        // All Statements
        LatestStatementSection(liabilityState, onStatementClick)

        // Unbilled Transactions
        UnbilledTransactionsSection(liabilityState, userCurrency, onTransactionClick)

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
                    text = CurrencyFormatter.format(liabilityState.latestStatement?.minimumPayment ?: 0.0, userCurrency),
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
    return "$prefix${CurrencyFormatter.format(displayAmount, userCurrency)}"
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
