package com.casha.app.ui.feature.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
        containerColor = Color(0xFFF8F9FA)
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Portfolio",
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
                        tint = Color(0xFF009033), // Matching LiabilitiesListScreen accent green
                        modifier = Modifier.size(45.dp) // Large icon as in Liabilites
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true },
                state = pullToRefreshState,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isLoading && uiState.assets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.assets.isEmpty()) {
                    EmptyStateView(onAddAssetClick = { showCreateAsset = true })
                } else {
                    AssetsList(
                        assets = uiState.assets,
                        summary = uiState.portfolioSummary,
                        onAssetClick = onNavigateToAssetDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetsList(
    assets: List<Asset>,
    summary: com.casha.app.domain.model.PortfolioSummary?,
    onAssetClick: (Asset) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header
        item {
            PortfolioSummaryHeader(
                summary = summary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        // Grouped Assets
        AssetCategory.entries.forEach { category ->
            val assetsInCategory = assets.filter { it.type.category == category }
            if (assetsInCategory.isNotEmpty()) {
                // Category Header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = mapSFSymbolToImageVector(category.icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.rawValue,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Asset Rows
                items(assetsInCategory) { asset ->
                    Surface(
                        onClick = { onAssetClick(asset) },
                        shape = MaterialTheme.shapes.medium,
                        color = Color.White,
                        tonalElevation = 1.dp
                    ) {
                        AssetRow(
                            asset = asset,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(onAddAssetClick: () -> Unit) {
    Column(
        modifier = Modifier
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

/**
 * Maps SwiftUI SF Symbol names used in AssetCategory.icon to Android Material Icons.
 */
private fun mapSFSymbolToImageVector(sfSymbol: String): ImageVector {
    return Icons.AutoMirrored.Filled.ArrowBack
}
