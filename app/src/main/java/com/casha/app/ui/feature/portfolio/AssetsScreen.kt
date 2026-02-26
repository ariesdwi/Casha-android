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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.domain.model.Asset
import com.casha.app.domain.model.AssetCategory
import com.casha.app.ui.feature.portfolio.subviews.AssetRow
import com.casha.app.ui.feature.portfolio.subviews.PortfolioSummaryHeader

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
    
    var showCreateAsset by remember { mutableStateOf(false) }
    var assetToEdit by remember { mutableStateOf<Asset?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Handlers for modals
    val onDismissCreate = { showCreateAsset = false }
    val onDismissEdit = { assetToEdit = null }

    if (showCreateAsset) {
        CreateAssetScreen(
            viewModel = viewModel,
            onNavigateBack = onDismissCreate,
            onSuccess = { newAsset ->
                showCreateAsset = false
                onNavigateToAssetDetail(newAsset)
            }
        )
    }

    assetToEdit?.let { asset ->
        EditAssetScreen(
            asset = asset,
            viewModel = viewModel,
            onNavigateBack = onDismissEdit
        )
    }
    
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.fetchAssets()
            viewModel.fetchPortfolioSummary(force = true)
            isRefreshing = false
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.fetchAssets()
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
            // Custom Header matching LiabilitiesListScreen style
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // using AutoMirrored for RTL support
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Portofolio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { showCreateAsset = true },
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Asset",
                        tint = Color(0xFF009033),
                        modifier = Modifier.size(45.dp)
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
                            onAddAssetClick = { showCreateAsset = true },
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
        // Summary Header
        item {
            PortfolioSummaryHeader(
                summary = summary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Grouped Assets
        AssetCategory.entries.forEach { category ->
            val assetsInCategory = assets.filter { it.type.category == category }
            if (assetsInCategory.isNotEmpty()) {
                // Category Header
                item {
                    SectionHeader(
                        icon = mapSFSymbolToImageVector(category.icon),
                        title = category.rawValue,
                        iconTint = Color(0xFF009033) // Match secondary style tint if needed, or default
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
        Icon(
            imageVector = Icons.Default.PieChart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Belum Ada Aset",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Mulai pantau kekayaan bersih Anda dengan menambahkan aset pertama Anda.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAddAssetClick,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.height(50.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah Aset")
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
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
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Maps SwiftUI SF Symbol names used in AssetCategory.icon to Android Material Icons.
 */
private fun mapSFSymbolToImageVector(sfSymbol: String): ImageVector {
    return Icons.AutoMirrored.Filled.ArrowBack
}
