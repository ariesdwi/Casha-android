package com.casha.app.ui.feature.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.R
import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetCategory
import com.casha.app.ui.feature.portfolio.subviews.AssetRow
import com.casha.app.ui.feature.portfolio.subviews.PortfolioSummaryCard
import com.casha.app.ui.util.mapSFSymbolToImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    viewModel: PortfolioViewModel,
    onNavigateToAssetDetail: (Asset) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    
    var showCategoryPicker by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<AssetCategory?>(null) }
    var showCreateAsset by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Handlers for modals
    val onDismissCreate = { 
        showCreateAsset = false 
        selectedCategory = null
    }

    if (showCategoryPicker) {
        SelectAssetCategoryScreen(
            onNavigateBack = { showCategoryPicker = false },
            onCategorySelected = { category ->
                selectedCategory = category
                showCategoryPicker = false
                showCreateAsset = true
            }
        )
    }

    if (showCreateAsset && selectedCategory != null) {
        CreateAssetScreen(
            selectedCategory = selectedCategory!!,
            viewModel = viewModel,
            onNavigateBack = onDismissCreate,
            onSuccess = { newAsset ->
                onDismissCreate()
                onNavigateToAssetDetail(newAsset)
            }
        )
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.fetchPortfolioSummary(force = true)
            isRefreshing = false
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.fetchPortfolioSummary()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // Toolbar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Portfolio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { showCategoryPicker = true }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Asset",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { isRefreshing = true },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.isLoading && uiState.assets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (uiState.assets.isEmpty()) {
                        EmptyStateView(
                            onAddAssetClick = { showCategoryPicker = true },
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    } else {
                        AssetsList(
                            assets = uiState.assets,
                            summary = uiState.portfolioSummary,
                            onAssetClick = onNavigateToAssetDetail,
                            state = scrollState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetsList(
    assets: List<Asset>,
    summary: com.casha.app.domain.model.PortfolioSummary?,
    onAssetClick: (Asset) -> Unit,
    state: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Card
        if (summary != null) {
            item {
                PortfolioSummaryCard(
                    summary = summary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        
        // Grouped Assets
        AssetCategory.entries.forEach { category ->
            val assetsInCategory = assets.filter { it.type.category == category }
            if (assetsInCategory.isNotEmpty()) {
                // Category Header
                item {
                    val categoryColorMap = mapOf(
                        AssetCategory.LIQUID to Color(0xFF2196F3),
                        AssetCategory.EQUITY to Color(0xFF9C27B0),
                        AssetCategory.FIXED_INCOME to Color(0xFF009688),
                        AssetCategory.COMMODITIES to Color(0xFFFF9800),
                        AssetCategory.CRYPTO to Color(0xFFFFEB3B),
                        AssetCategory.REAL_ESTATE to Color(0xFF4CAF50),
                        AssetCategory.VEHICLES to Color(0xFF3F51B5),
                        AssetCategory.BUSINESS to Color(0xFFE91E63),
                        AssetCategory.OTHERS to Color(0xFF9E9E9E)
                    )
                    
                    SectionHeader(
                        icon = mapSFSymbolToImageVector(category.icon),
                        title = category.rawValue,
                        iconBgColor = categoryColorMap[category] ?: Color.Gray
                    )
                }
                
                // Asset Items
                items(assetsInCategory, key = { "asset-${it.id}" }) { asset ->
                    AssetRow(
                        asset = asset,
                        onClick = { onAssetClick(asset) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    onAddAssetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF1565C0).copy(alpha = 0.15f), Color(0xFF0097A7).copy(alpha = 0.15f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = Color(0xFF1565C0).copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.portfolio_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.portfolio_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Gradient Add Button
            Button(
                onClick = onAddAssetClick,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 180.dp)
                        .height(52.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF1565C0), Color(0xFF0097A7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Text(stringResource(R.string.portfolio_action_add_asset), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    iconBgColor: Color
) {
    Row(
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = Color.White
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


