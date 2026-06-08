package com.casha.app.ui.feature.transaction.subview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.casha.app.R
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
import com.casha.app.domain.model.DisplaySegment
import com.casha.app.ui.feature.transaction.CashflowUiUtils
import com.casha.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card displaying a section of transactions for a specific date.
 * 
 * @param section The cashflow date section to display
 * @param onClick Callback when an item is clicked (id, type)
 * @param onGroupClick Callback when a grouped transaction is clicked
 * @param onEdit Callback when edit is selected from context menu (id, type)
 * @param onDelete Callback when delete is selected from context menu (id, type)
 * @param modifier Optional modifier
 */
@Composable
fun TransactionSectionCard(
    section: CashflowDateSection,
    onClick: (String, String) -> Unit = { _, _ -> },
    onGroupClick: (String) -> Unit = {},
    onEdit: ((String, String) -> Unit)? = null,
    onDelete: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    
    val totalIncome = section.items.filter { it.type == CashflowType.INCOME }.sumOf { it.amount }
    val totalExpense = section.items.filter { it.type == CashflowType.EXPENSE }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    // Use segments if available, otherwise fall back to individual items
    val segments = section.segments.ifEmpty {
        section.items.map { DisplaySegment.Individual(it) }
    }

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
                        text = stringResource(R.string.transactions_net),
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
                    segments.forEachIndexed { index, segment ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        when (segment) {
                            is DisplaySegment.Individual -> {
                                TransactionListItem(
                                    entry = segment.entry,
                                    onClick = { onClick(segment.entry.id, segment.entry.type.name) },
                                    onEdit = if (onEdit != null) {
                                        { onEdit(segment.entry.id, segment.entry.type.name) }
                                    } else null,
                                    onDelete = if (onDelete != null) {
                                        { onDelete(segment.entry.id, segment.entry.type.name) }
                                    } else null
                                )
                            }
                            is DisplaySegment.Grouped -> {
                                GroupTransactionRow(
                                    group = segment,
                                    onClick = { onGroupClick(segment.groupId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual transaction list item with context menu support.
 * 
 * Task 10.1: Add context menu support with long-press gesture
 * Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6
 * 
 * @param entry The cashflow entry to display
 * @param onClick Callback when item is tapped (navigates to detail)
 * @param onEdit Callback when edit is selected from context menu
 * @param onDelete Callback when delete is selected from context menu
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListItem(
    entry: CashflowEntry,
    onClick: () -> Unit = {},
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Context menu state
    var showContextMenu by remember { mutableStateOf(false) }
    
    // Determine if transaction is synced (for now, assume all are synced)
    // In real implementation, this would come from the entry data
    val isSynced = true // TODO: Replace with actual sync state from entry

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (onEdit != null || onDelete != null) {
                        showContextMenu = true
                    }
                }
            )
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

        // Name and Category
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = entry.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount and Time
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val amountColor = CashflowUiUtils.colorForType(entry.type)
            val prefix = if (entry.type == CashflowType.INCOME) "+" else "-"

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = CurrencyFormatter.format(entry.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
            }
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(entry.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Context Menu Dropdown
    if (showContextMenu) {
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            // Edit option
            if (onEdit != null) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isSynced) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                            Text(
                                text = "Edit",
                                color = if (isSynced) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    },
                    onClick = {
                        if (isSynced) {
                            showContextMenu = false
                            onEdit()
                        }
                    },
                    enabled = isSynced
                )
            }
            
            // Delete option
            if (onDelete != null) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = if (isSynced) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                            )
                            Text(
                                text = "Delete",
                                color = if (isSynced) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                            )
                        }
                    },
                    onClick = {
                        if (isSynced) {
                            showContextMenu = false
                            onDelete()
                        }
                    },
                    enabled = isSynced
                )
            }
        }
    }
}
