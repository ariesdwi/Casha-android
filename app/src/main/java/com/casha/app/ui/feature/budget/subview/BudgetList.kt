package com.casha.app.ui.feature.budget.subview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.casha.app.domain.model.BudgetCasha
import com.casha.app.domain.model.BudgetSummary

@Composable
fun BudgetList(
    budgets: List<BudgetCasha>,
    summary: BudgetSummary?,
    isLoading: Boolean,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    onNavigateToAIRecommendations: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        BudgetShimmerList(modifier = modifier)
    } else if (budgets.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BudgetOverviewCard(summary = summary)
                BudgetEmptyState(onNavigateToAIRecommendations = onNavigateToAIRecommendations)
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary item
            item {
                BudgetOverviewCard(summary = summary)
            }

            items(budgets, key = { it.id }) { budget ->
                BudgetCardItem(
                    budget = budget,
                    onDelete = { onDelete(budget.id) },
                    onEdit = { onEdit(budget.id) }
                )
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun BudgetEmptyState(
    onNavigateToAIRecommendations: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No budgets set yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Let AI help you create a personalized budget plan based on your income.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onNavigateToAIRecommendations,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Get AI Recommendations")
            }
        }
    }
}
