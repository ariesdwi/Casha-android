package com.casha.app.ui.feature.liability

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.domain.model.Liability
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun LiabilitiesListScreen(
    viewModel: LiabilityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchLiabilities()
        viewModel.fetchLiabilitySummary()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToCreate,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 2.dp, shape = androidx.compose.foundation.shape.CircleShape)
                            .background(Color.White, androidx.compose.foundation.shape.CircleShape) // Background circle
                    ) {
                         Icon(
                             imageVector = androidx.compose.material.icons.Icons.Default.AddCircle,
                             contentDescription = "Add Liability",
                             tint = Color(0xFF009033), // The specific green color
                             modifier = Modifier.size(45.dp) // The icon inherently creates its own circle when large
                         )
                    }
                }

                Text(
                    text = "Liabilities",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading && uiState.liabilities.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.liabilities.isEmpty() -> {
                    EmptyStateView(onCreateClick = onNavigateToCreate)
                }
                else -> {
                    LiabilitiesList(
                        liabilities = uiState.liabilities,
                        summary = uiState.liabilitySummary,
                        isLoading = uiState.isLoading,
                        onLiabilityClick = onNavigateToDetail,
                        onSummaryClick = {}
                    )
                }
                }
            }
        }
    }
}
}

@Composable
private fun LiabilitiesList(
    liabilities: List<Liability>,
    summary: com.casha.app.domain.model.LiabilitySummary?,
    isLoading: Boolean,
    onLiabilityClick: (String) -> Unit,
    onSummaryClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LiabilitySummaryCard(
                summary = summary,
                isLoading = isLoading,
                onTap = onSummaryClick
            )
        }

        item {
            Text(
                text = "Active Liabilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(liabilities, key = { it.id }) { liability ->
            LiabilityRow(
                liability = liability,
                onClick = { onLiabilityClick(liability.id) }
            )
        }
    }
}

@Composable
private fun EmptyStateView(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "No Liabilities",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add your credit cards, loans, or mortgages to start tracking your debt.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(text = "Add Liability", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
