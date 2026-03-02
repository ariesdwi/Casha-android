package com.casha.app.ui.feature.liability

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.domain.model.Liability
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun LiabilitiesListScreen(
    viewModel: LiabilityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchAllLiabilities()
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
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.liabilities_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = onNavigateToCreate,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 2.dp, shape = CircleShape)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.liabilities_action_add),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(45.dp)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        uiState.isLoading && uiState.activeLiabilities.isEmpty() && uiState.paidOffLiabilities.isEmpty() -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        uiState.activeLiabilities.isEmpty() && uiState.paidOffLiabilities.isEmpty() -> {
                            EmptyStateView(onCreateClick = onNavigateToCreate)
                        }
                        else -> {
                            LiabilitiesList(
                                uiState = uiState,
                                onLiabilityClick = onNavigateToDetail,
                                onSummaryClick = {},
                                onDeleteClick = { id ->
                                    viewModel.deleteLiability(id, onSuccess = {})
                                }
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
    uiState: LiabilityState,
    onLiabilityClick: (String) -> Unit,
    onSummaryClick: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary card
        item {
            LiabilitySummaryCard(
                totalBalance = uiState.totalBalance,
                totalMonthlyInstallment = uiState.totalMonthlyInstallment,
                activeCount = uiState.activeCount,
                paidOffCount = uiState.paidOffCount,
                isLoading = uiState.isLoading,
                onTap = onSummaryClick
            )
        }

        // Overdue section
        if (uiState.overdueLiabilities.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.Error,
                    title = stringResource(R.string.liabilities_section_overdue),
                    iconTint = Color(0xFFE53935)
                )
            }

            items(uiState.overdueLiabilities, key = { "overdue-${it.id}" }) { liability ->
                SwipeToDeleteLiabilityRow(
                    liability = liability,
                    onClick = { onLiabilityClick(liability.id) },
                    onDelete = { onDeleteClick(liability.id) }
                )
            }
        }

        // Active (non-overdue) section
        if (uiState.nonOverdueLiabilities.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.CreditCard,
                    title = stringResource(R.string.liabilities_section_active),
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.nonOverdueLiabilities, key = { "active-${it.id}" }) { liability ->
                SwipeToDeleteLiabilityRow(
                    liability = liability,
                    onClick = { onLiabilityClick(liability.id) },
                    onDelete = { onDeleteClick(liability.id) }
                )
            }
        }

        // Paid-off section
        if (uiState.paidOffLiabilities.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Default.CheckCircle,
                    title = stringResource(R.string.liabilities_section_paid_off),
                    iconTint = Color(0xFF4CAF50)
                )
            }

            items(uiState.paidOffLiabilities, key = { "paidoff-${it.id}" }) { liability ->
                SwipeToDeleteLiabilityRow(
                    liability = liability,
                    onClick = { onLiabilityClick(liability.id) },
                    onDelete = { onDeleteClick(liability.id) }
                )
            }
        }
    }
}

@Composable
private fun SwipeToDeleteLiabilityRow(
    liability: Liability,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val actionButtonWidth = 80.dp
    val actionButtonWidthPx = with(density) { actionButtonWidth.toPx() }
    val fullSwipeThresholdPx = with(density) { 200.dp.toPx() }
    
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Derived animation values based on swipe progress
    val swipeProgress = (abs(offsetX.value) / actionButtonWidthPx).coerceIn(0f, 1.5f)
    val deleteButtonScale = (swipeProgress * 1.1f).coerceIn(0f, 1f)
    val deleteButtonAlpha = (swipeProgress * 1.3f).coerceIn(0f, 1f)
    val isOverThreshold = abs(offsetX.value) > fullSwipeThresholdPx
    // Subtle red tint intensity based on swipe distance
    val backgroundAlpha = (swipeProgress * 0.15f).coerceIn(0f, 0.12f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        // Background — subtle gradient that intensifies on swipe
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFF44336).copy(alpha = backgroundAlpha)
                        )
                    )
                )
                .padding(end = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Animated delete button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .width(actionButtonWidth)
                    .scale(deleteButtonScale)
                    .alpha(deleteButtonAlpha)
                    .clickable {
                        coroutineScope.launch {
                            // Animate card sliding away, then delete
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                            onDelete()
                        }
                    }
                    .padding(vertical = 8.dp)
            ) {
                // Red circle — grows slightly when over threshold
                val circleScale = if (isOverThreshold) 1.15f else 1f
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(circleScale)
                        .shadow(
                            elevation = if (swipeProgress > 0.3f) 6.dp else 0.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFFF44336),
                            spotColor = Color(0xFFF44336)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFEF5350),
                                    Color(0xFFD32F2F)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.liabilities_action_delete),
                    color = Color(0xFF9E9E9E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Draggable card content with subtle elevation during swipe
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .shadow(
                    elevation = if (swipeProgress > 0.1f) (swipeProgress * 4).dp else 0.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount)
                                .coerceIn(-fullSwipeThresholdPx - 100f, 0f)
                            coroutineScope.launch {
                                offsetX.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value < -fullSwipeThresholdPx) {
                                    // Full swipe — animate out and delete
                                    offsetX.animateTo(
                                        targetValue = -size.width.toFloat(),
                                        animationSpec = tween(durationMillis = 250)
                                    )
                                    onDelete()
                                } else if (offsetX.value < -actionButtonWidthPx / 2) {
                                    // Snap open with spring
                                    offsetX.animateTo(
                                        targetValue = -actionButtonWidthPx,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                } else {
                                    // Snap closed with spring
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
        ) {
            LiabilityRow(
                liability = liability,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
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
            text = stringResource(R.string.liabilities_empty_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.liabilities_empty_subtitle),
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
                Text(text = stringResource(R.string.liabilities_action_add), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
