package com.casha.app.ui.feature.budget.subview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.stringResource
import com.casha.app.R

@Composable
private fun getProgressColor(progress: Float): Color {
    val isDark = isSystemInDarkTheme()
    return when {
        progress < 0.7f -> if (isDark) Color(0xFF81C784) else CashaSuccess
        progress < 0.9f -> if (isDark) Color(0xFFFFB74D) else CashaWarning
        else -> if (isDark) Color(0xFFE57373) else CashaDanger
    }
}

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
            title = { Text(text = stringResource(R.string.budget_action_delete)) },
            text = { Text(text = stringResource(R.string.budget_delete_confirmation, budget.category)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CashaDanger)
                ) {
                    Text(stringResource(R.string.budget_action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.budget_action_cancel))
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
    val isDark = isSystemInDarkTheme()
    val dangerColor = if (isDark) Color(0xFFE57373) else CashaDanger

    val color by animateColorAsState(
        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
            dangerColor
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
    val progressColor = getProgressColor(progress)
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
    val progressColor = getProgressColor(progress)

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
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
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
    val progressColor = getProgressColor(progress)
    val progressPercentage = "${(progress * 100).toInt()}%"

    val isDark = isSystemInDarkTheme()
    val successColor = if (isDark) Color(0xFF81C784) else CashaSuccess
    val dangerColor = if (isDark) Color(0xFFE57373) else CashaDanger

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FooterItem(
            label = stringResource(R.string.budget_label_spent),
            value = CurrencyFormatter.format(budget.spent),
            valueColor = MaterialTheme.colorScheme.onSurface,
            alignment = Alignment.Start
        )
        
        FooterItem(
            label = stringResource(R.string.budget_label_progress),
            value = progressPercentage,
            valueColor = progressColor,
            alignment = Alignment.CenterHorizontally
        )
        
        FooterItem(
            label = stringResource(R.string.budget_label_remaining),
            value = CurrencyFormatter.format(budget.remaining),
            valueColor = if (budget.remaining >= 0) successColor else dangerColor,
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
