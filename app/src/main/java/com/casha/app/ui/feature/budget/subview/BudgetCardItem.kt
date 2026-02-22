package com.casha.app.ui.feature.budget.subview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.CurrencyFormatter
import com.casha.app.core.util.DateHelper
import com.casha.app.domain.model.BudgetCasha
import com.casha.app.ui.theme.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCardItem(
    budget: BudgetCasha,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
                false
            } else {
                false
            }
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete Budget") },
            text = { Text(text = "Are you sure you want to delete the budget for ${budget.category}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CashaDanger)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            BudgetSwipeBackground(dismissState)
        },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            onClick = onEdit
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BudgetHeaderPart(budget)
                BudgetProgressPart(budget)
                BudgetFooterPart(budget)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetSwipeBackground(dismissState: SwipeToDismissBoxState) {
    val color by animateColorAsState(
        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
            CashaDanger
        } else {
            Color.Transparent
        },
        label = "SwipeBackground"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(color),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (dismissState.progress > 0.1f) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 20.dp)
                    .size(24.dp)
            )
        }
    }
}

@Composable
private fun BudgetHeaderPart(budget: BudgetCasha) {
    val progress = if (budget.amount > 0) min(budget.spent / budget.amount, 1.0).toFloat() else 0f
    val progressColor = when {
        progress < 0.7f -> CashaSuccess
        progress < 0.9f -> CashaWarning
        else -> CashaDanger
    }
    val progressPercentage = "${(progress * 100).toInt()}%"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = budget.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${DateHelper.formatDisplay(budget.startDate)} - ${DateHelper.formatDisplay(budget.endDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyFormatter.format(budget.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Surface(
                color = progressColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = progressPercentage,
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressPart(budget: BudgetCasha) {
    val progress = if (budget.amount > 0) min(budget.spent / budget.amount, 1.0).toFloat() else 0f
    val progressColor = when {
        progress < 0.7f -> CashaSuccess
        progress < 0.9f -> CashaWarning
        else -> CashaDanger
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "BudgetProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(Color(0xFFF0F0F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(CircleShape)
                .background(progressColor)
        )
        
        // Threshold indicators (notches)
        Row(modifier = Modifier.fillMaxSize()) {
            repeat(4) { index ->
                if (index > 0) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.5.dp)
                            .background(Color.White.copy(alpha = 0.5f))
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BudgetFooterPart(budget: BudgetCasha) {
    val progress = if (budget.amount > 0) min(budget.spent / budget.amount, 1.0).toFloat() else 0f
    val progressColor = when {
        progress < 0.7f -> CashaSuccess
        progress < 0.9f -> CashaWarning
        else -> CashaDanger
    }
    val progressPercentage = "${(progress * 100).toInt()}%"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FooterItem(
            label = "Spent",
            value = CurrencyFormatter.format(budget.spent),
            valueColor = MaterialTheme.colorScheme.onSurface,
            alignment = Alignment.Start
        )
        
        FooterItem(
            label = "Progress",
            value = progressPercentage,
            valueColor = progressColor,
            alignment = Alignment.CenterHorizontally
        )
        
        FooterItem(
            label = "Remaining",
            value = CurrencyFormatter.format(budget.remaining),
            valueColor = CashaSuccess,
            alignment = Alignment.End
        )
    }
}

@Composable
private fun FooterItem(
    label: String,
    value: String,
    valueColor: Color,
    alignment: Alignment.Horizontal
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}
