package com.casha.app.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.R
import com.casha.app.domain.model.NotificationCasha
import com.casha.app.core.util.CurrencyFormatter
import kotlinx.coroutines.delay

@Composable
fun BudgetAlertBanner(
    notification: NotificationCasha,
    onTap: () -> Unit,
    onDismiss: () -> Unit
) {
    val notifCategory = notification.data["category"] ?: "Category"
    val spendString = notification.data["currentSpent"] ?: "0"
    val budgetString = notification.data["budgetAmount"] ?: "0"
    val currentSpent = spendString.toDoubleOrNull() ?: 0.0
    val budgetAmount = budgetString.toDoubleOrNull() ?: 0.0
    
    val percentageString = notification.data["percentage"] ?: "0"
    val percentage = percentageString.toDoubleOrNull() ?: 0.0
    
    val isOverBudget = percentage > 100.0

    // Temporary offset for drag-to-dismiss gesture
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .offset(y = offsetY.dp)
            .draggable(
                state = rememberDraggableState { delta ->
                    if (offsetY + delta < 0) {
                        offsetY += delta
                    }
                },
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    if (offsetY < -50f) {
                        onDismiss()
                    } else {
                        offsetY = 0f
                    }
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Outer shadow/background layer
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
                .clickable { onTap() }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            // Header: Icon + Title/Body + Dismiss Button
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circle Warning Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Red.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.Gray.copy(alpha = 0.1f), CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            // Budget Progress Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category & Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (notifCategory == "Category") stringResource(R.string.category) else notifCategory,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isOverBudget) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (percentage / 100.0).coerceIn(0.0, 1.0).toFloat())
                            .height(8.dp)
                            .background(
                                color = if (isOverBudget) Color.Red else Color(0xFFFFA500), // Orange
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
                
                // Spent of Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CurrencyFormatter.format(currentSpent),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.budget_banner_of, CurrencyFormatter.format(budgetAmount)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
