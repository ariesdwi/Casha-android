package com.casha.app.ui.feature.liability

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.Liability
import com.casha.app.domain.model.LiabilityStatement
import com.casha.app.domain.model.LiabilityTransaction
import com.casha.app.domain.model.TransactionCategory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementDetailView(
    statement: LiabilityStatement,
    liability: Liability,
    liabilityState: LiabilityState,
    onBack: () -> Unit,
    onFetchStatementDetails: (String, String) -> Unit,
    onTransactionClick: (LiabilityTransaction) -> Unit
) {
    val scrollState = rememberScrollState()
    val userCurrency = CurrencyFormatter.defaultCurrency
    
    val currentStatement = liabilityState.selectedStatementDetail ?: statement

    LaunchedEffect(statement.id) {
        onFetchStatementDetails(liability.id, statement.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("View Statement", fontSize = 18.sp) }, // Should use stringResource in prod
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Statement Period Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Statement Period",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatStatementDetailDate(currentStatement.startDate)} - ${formatStatementDetailDate(currentStatement.endDate)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Warnings Section
            if (currentStatement.warnings.isNotEmpty()) {
                currentStatement.warnings.forEach { warning ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFA500).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFFA500))
                        Text(
                            text = warning,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Statement Breakdown Section
            BreakdownSection(currentStatement, liability, userCurrency)

            // Transactions Section
            TransactionsSection(liabilityState, userCurrency, onTransactionClick)
        }
    }
}

@Composable
private fun BreakdownSection(statement: LiabilityStatement, liability: Liability, userCurrency: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Statement Breakdown",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatementRow("Previous Outstanding", statement.previousBalance, MaterialTheme.colorScheme.onSurface, userCurrency)
                StatementRow("New Transactions", statement.purchasesMade, MaterialTheme.colorScheme.onSurface, userCurrency)
                StatementRow("Interest", statement.interestCharged, MaterialTheme.colorScheme.onSurface, userCurrency)
                
                if (statement.lateFeesCharged > 0) {
                    StatementRow("Late Fee", statement.lateFeesCharged, Color.Red, userCurrency)
                }

                StatementRow("Payments", -statement.paymentsMade, Color.Green, userCurrency)

                Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Billed", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatStatementDetailCurrency(statement.statementBalance, userCurrency), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Minimum Payment (${liability.minPaymentPercentage?.toInt() ?: 5}%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatStatementDetailCurrency(statement.minimumPayment, userCurrency), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA500))
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = "Due Date", tint = Color.Red, modifier = Modifier.size(16.dp))
                    Text("Due Date: ${formatStatementDetailDate(statement.dueDate)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
        }
    }
}

@Composable
private fun StatementRow(label: String, amount: Double, color: Color, userCurrency: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = formatStatementRowCurrency(amount, userCurrency),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TransactionsSection(liabilityState: LiabilityState, userCurrency: String, onTransactionClick: (LiabilityTransaction) -> Unit) {
    val transactions = liabilityState.selectedStatementDetail?.transactions ?: emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Transactions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("${transactions.size} transactions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (liabilityState.isLoading && transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (transactions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    transactions.forEachIndexed { index, transaction ->
                        TransactionRowView(transaction, userCurrency, onClick = { onTransactionClick(transaction) })
                        if (index < transactions.size - 1) {
                            Divider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No transactions for this period.",
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TransactionRowView(transaction: LiabilityTransaction, userCurrency: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color.Blue.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Replaced custom icon mapping with hardcoded icon since transactionCategory icons vary wildly in Android vs Swift Native
            Text(text = transaction.name.firstOrNull()?.uppercase() ?: "T", color = Color.Blue, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = transaction.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = transaction.category?.name ?: "Other", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = formatTransactionDate(transaction.datetime), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Text(
            text = formatStatementDetailCurrency(transaction.amount, userCurrency),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isTransactionCredit(transaction)) Color.Green else Color.Red
        )
    }
}

internal fun formatStatementDetailCurrency(amount: Double, currencyCode: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.currency = Currency.getInstance(currencyCode)
    format.maximumFractionDigits = 0
    return format.format(amount).replace("Rp", "Rp ")
}

internal fun formatStatementRowCurrency(amount: Double, currencyCode: String): String {
    val absAmount = Math.abs(amount)
    val formatted = formatStatementDetailCurrency(absAmount, currencyCode)
    return if (amount < 0) "- $formatted" else formatted
}

internal fun formatStatementDetailDate(date: Date): String {
    return try {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        date.toString()
    }
}

internal fun formatTransactionDate(date: Date): String {
    return try {
        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        date.toString()
    }
}

private fun isTransactionCredit(transaction: LiabilityTransaction): Boolean {
    if (transaction.type?.uppercase(Locale.getDefault()) == "CREDIT") {
        return true
    }
    return transaction.amount < 0
}
