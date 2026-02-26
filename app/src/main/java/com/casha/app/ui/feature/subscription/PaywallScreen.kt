package com.casha.app.ui.feature.subscription

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.casha.app.R
import com.casha.app.ui.theme.CashaPrimaryLight
import kotlinx.coroutines.delay

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    viewModel: SubscriptionManager = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPurchasing by viewModel.isPurchasing.collectAsState()
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedProductId by remember { mutableStateOf<String?>(null) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var purchaseSuccess by remember { mutableStateOf(false) }
    var appearAnimation by remember { mutableStateOf(false) }

    val lifetimeProduct = products.find { it.productId == "premium.casha.lifetime" }
    val monthlyProduct = products.find { it.productId == "premium.casha.monthly" }

    // Initialize selection
    LaunchedEffect(products) {
        if (selectedProductId == null && products.isNotEmpty()) {
            selectedProductId = lifetimeProduct?.productId ?: monthlyProduct?.productId
        }
    }

    // Success handling
    LaunchedEffect(hasPremiumAccess) {
        if (hasPremiumAccess && purchaseSuccess) {
            showSuccessAnimation = true
            delay(2000)
            onDismiss()
        }
    }

    // Appear animation
    LaunchedEffect(Unit) {
        delay(100)
        appearAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background
        BackgroundView()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .blur(if (isPurchasing) 10.dp else 0.dp)
        ) {
            HeaderSection(appearAnimation)

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                FeatureSection(appearAnimation)
                
                PlansSection(
                    lifetimeProduct = lifetimeProduct,
                    monthlyProduct = monthlyProduct,
                    selectedProductId = selectedProductId,
                    onSelect = { selectedProductId = it }
                )

                ActionButton(
                    isPurchasing = isPurchasing,
                    enabled = selectedProductId != null,
                    label = getActionButtonLabel(isPurchasing, products.find { it.productId == selectedProductId }),
                    onClick = {
                        val product = products.find { it.productId == selectedProductId }
                        if (product != null && activity != null) {
                            purchaseSuccess = false
                            viewModel.purchase(activity, product)
                            // We assume success if hasPremiumAccess changes to true later
                            purchaseSuccess = true 
                        }
                    }
                )

                FooterLinks(
                    onRestore = { viewModel.restorePurchases() },
                    onTerms = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cashaapp.my.id/terms"))
                        context.startActivity(intent)
                    },
                    onPrivacy = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cashaapp.my.id/privacy"))
                        context.startActivity(intent)
                    }
                )
                
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Close Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(32.dp)
                .background(Color.Black.copy(alpha = 0.05f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Overlays
        AnimatedVisibility(
            visible = isPurchasing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PurchaseOverlay()
        }

        AnimatedVisibility(
            visible = showSuccessAnimation,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut()
        ) {
            SuccessOverlay()
        }
        
        // Error Snackbar
        errorMessage?.let { msg ->
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss", color = Color.White)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(msg)
                }
            }
        }
    }
}

@Composable
fun BackgroundView() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CashaPrimaryLight.copy(alpha = 0.15f),
                            CashaPrimaryLight.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Aesthetic blobs
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-150).dp, y = (-200).dp)
                .blur(80.dp)
                .background(CashaPrimaryLight.copy(alpha = 0.12f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 150.dp, y = 100.dp)
                .blur(60.dp)
                .background(Color(0xFFFFA000).copy(alpha = 0.06f), CircleShape)
        )
    }
}

@Composable
fun HeaderSection(visible: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val opacity by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1000),
        label = "opacity"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 40.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "offsetY"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = opacity
                }
                .background(CashaPrimaryLight.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = CashaPrimaryLight,
                modifier = Modifier.size(56.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .offset(y = offsetY)
                .graphicsLayer { alpha = opacity }
        ) {
            Text(
                text = stringResource(R.string.subscription_title_get_premium),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                letterSpacing = (-1).sp
            )
            Text(
                text = stringResource(R.string.subscription_subtitle_join_users),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun FeatureSection(visible: Boolean) {
    val opacity by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, delayMillis = 400),
        label = "opacity"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = opacity }
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FeatureRow(
            icon = Icons.Default.AutoAwesome,
            title = stringResource(R.string.subscription_feature_ai_advisor_title),
            subtitle = stringResource(R.string.subscription_feature_ai_advisor_subtitle)
        )
        FeatureRow(
            icon = Icons.Default.DocumentScanner,
            title = stringResource(R.string.subscription_feature_receipt_scanner_title),
            subtitle = stringResource(R.string.subscription_feature_receipt_scanner_subtitle)
        )
        FeatureRow(
            icon = Icons.Default.PieChart,
            title = stringResource(R.string.subscription_feature_advanced_analytics_title),
            subtitle = stringResource(R.string.subscription_feature_advanced_analytics_subtitle)
        )
        FeatureRow(
            icon = Icons.Default.NotificationsActive,
            title = stringResource(R.string.subscription_feature_smart_alerts_title),
            subtitle = stringResource(R.string.subscription_feature_smart_alerts_subtitle)
        )
    }
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CashaPrimaryLight,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PlansSection(
    lifetimeProduct: ProductDetails?,
    monthlyProduct: ProductDetails?,
    selectedProductId: String?,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        lifetimeProduct?.let {
            SubscriptionPlanCard(
                product = it,
                isSelected = selectedProductId == it.productId,
                isBestValue = true,
                onClick = { onSelect(it.productId) }
            )
        }
        monthlyProduct?.let {
            SubscriptionPlanCard(
                product = it,
                isSelected = selectedProductId == it.productId,
                isBestValue = false,
                onClick = { onSelect(it.productId) }
            )
        }
    }
}

@Composable
fun ActionButton(
    isPurchasing: Boolean,
    enabled: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled && !isPurchasing,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = CashaPrimaryLight,
        contentColor = Color.White,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FooterLinks(
    onRestore: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.subscription_link_restore),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.clickable { onRestore() }
            )
            Box(modifier = Modifier.width(1.dp).height(10.dp).background(Color.LightGray))
            Text(
                text = stringResource(R.string.subscription_link_terms),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.clickable { onTerms() }
            )
            Box(modifier = Modifier.width(1.dp).height(10.dp).background(Color.LightGray))
            Text(
                text = stringResource(R.string.subscription_link_privacy),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.clickable { onPrivacy() }
            )
        }
        
        Text(
            text = stringResource(R.string.subscription_footer_cancel_anytime),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun PurchaseOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            Text(
                text = stringResource(R.string.subscription_overlay_securing_access),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SuccessOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Green.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(60.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.subscription_success_welcome),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.subscription_success_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

private fun getActionButtonLabel(isPurchasing: Boolean, selectedProduct: ProductDetails?): String {
    if (isPurchasing) return "Processing..."
    if (selectedProduct == null) return "Select Plan"
    return if (selectedProduct.productId.contains("lifetime")) "Get Lifetime Access" else "Start Free Trial"
}


