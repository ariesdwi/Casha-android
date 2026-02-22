package com.casha.app.ui.feature.transaction.subview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.domain.model.CashflowDateSection
import com.casha.app.domain.model.CashflowEntry
import com.casha.app.domain.model.CashflowType
import com.casha.app.ui.feature.transaction.CashflowUiUtils
import com.casha.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionSectionCard(
    section: CashflowDateSection,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    onClick: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    
    val totalIncome = section.items.filter { it.type == CashflowType.INCOME }.sumOf { it.amount }
    val totalExpense = section.items.filter { it.type == CashflowType.EXPENSE }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Side (Day, Date, Income/Expense summary)
                Column {
                    Text(
                        text = section.day,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = section.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Income
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = CashaSuccess,
                            modifier = Modifier
                                .size(14.dp)
                                .background(CashaSuccess.copy(alpha = 0.2f), CircleShape)
                                .padding(2.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = CurrencyFormatter.format(totalIncome),
                            style = MaterialTheme.typography.labelSmall,
                            color = CashaSuccess
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Expense
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = CashaDanger,
                            modifier = Modifier
                                .size(14.dp)
                                .background(CashaDanger.copy(alpha = 0.2f), CircleShape)
                                .padding(2.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = CurrencyFormatter.format(totalExpense),
                            style = MaterialTheme.typography.labelSmall,
                            color = CashaDanger
                        )
                    }
                }

                // Right Side (Net, Chevron)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val netColor = if (netBalance >= 0) MaterialTheme.colorScheme.onSurface else CashaDanger
                    val prefix = if (netBalance >= 0) "" else "-"
                    Text(
                        text = "$prefix${CurrencyFormatter.format(kotlin.math.abs(netBalance))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = netColor
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }

            // Items List
            AnimatedVisibility(visible = expanded) {
                Column {
                    section.items.forEachIndexed { index, entry ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        TransactionListItem(
                            entry = entry,
                            onEdit = { onEdit(entry.id) },
                            onDelete = { onDelete(entry.id) },
                            onClick = { onClick(entry.id, entry.type.name) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListItem(
    entry: CashflowEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (swipeState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                CashaDanger
            } else Color.Transparent

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        content = {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon with Gradient
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(CashflowUiUtils.gradientForType(entry.type), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CashflowUiUtils.iconForEntry(entry),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name and Type pill and Category
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (entry.type == CashflowType.INCOME) "Income" else "Expense",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .background(
                                    if (entry.type == CashflowType.INCOME) CashaSuccess else MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = entry.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount and Time
                Column(horizontalAlignment = Alignment.End) {
                    val amountColor = if (entry.type == CashflowType.INCOME) CashaSuccess else CashaDanger
                    val prefix = if (entry.type == CashflowType.INCOME) "+" else "-"

                    Text(
                        text = "$prefix${CurrencyFormatter.format(entry.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = amountColor
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(entry.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}
