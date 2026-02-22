package com.casha.app.ui.feature.transaction.subview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.casha.app.domain.model.CashflowEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionList(
    transactions: List<CashflowEntry>,
    isLoading: Boolean,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (transactions.isEmpty() && !isLoading) {
        EmptyTransactionsState(modifier)
    } else {
        val groupedTransactions = remember(transactions) {
            groupTransactionsByDate(transactions)
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedTransactions.forEach { (dateHeader, transactionList) ->
                item {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(transactionList, key = { it.id }) { entry ->
                    TransactionCardItem(
                        entry = entry,
                        onDelete = { onDelete(entry.id) },
                        onEdit = { onEdit(entry.id) }
                    )
                }
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun EmptyTransactionsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No transactions found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Start adding your daily incomes and expenses to see them here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

private fun groupTransactionsByDate(transactions: List<CashflowEntry>): Map<String, List<CashflowEntry>> {
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayFormatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    
    val today = dateFormatter.format(Date())
    val yesterday = dateFormatter.format(Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time)

    return transactions
        .sortedByDescending { it.date }
        .groupBy { dateFormatter.format(it.date) }
        .mapKeys { (dateKey, _) ->
            when (dateKey) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> {
                    val date = dateFormatter.parse(dateKey) ?: Date()
                    todayFormatter.format(date)
                }
            }
        }
}
