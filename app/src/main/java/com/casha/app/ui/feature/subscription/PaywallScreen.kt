package com.casha.app.ui.feature.subscription

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
        if (hasPremiumAccess && isPurchasing) {
            showSuccessAnimation = true
            delay(2000)
            onDismiss()
        }
    }

    // Appear animation
    LaunchedEffect(Unit) {
        appearAnimation = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        BackgroundView()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .blur(if (isPurchasing) 3.dp else 0.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            HeaderSection(appearAnimation)

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                FeatureSection()
                
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
                            viewModel.purchase(activity, product)
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
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Close Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
        }

        // Overlays
        if (isPurchasing) {
            PurchaseOverlay()
        }

        if (showSuccessAnimation) {
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
                    containerColor = MaterialTheme.colorScheme.error
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
                .background(MaterialTheme.colorScheme.background)
        )
        
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
                .blur(60.dp)
                .background(CashaPrimaryLight.copy(alpha = 0.1f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 150.dp, y = 100.dp)
                .blur(50.dp)
                .background(Color.Yellow.copy(alpha = 0.05f), CircleShape)
        )
    }
}

@Composable
fun HeaderSection(visible: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    val opacity by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "opacity"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = opacity
                }
                .background(CashaPrimaryLight.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star, // Using Star instead of Crown as crown-fill is SF Pro only
                contentDescription = null,
                tint = CashaPrimaryLight,
                modifier = Modifier.size(48.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 40.dp).graphicsLayer { this.alpha = opacity }
        ) {
            Text(
                text = stringResource(R.string.subscription_title_get_premium),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.subscription_subtitle_join_users),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun FeatureSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlansSection(
    lifetimeProduct: com.android.billingclient.api.ProductDetails?,
    monthlyProduct: com.android.billingclient.api.ProductDetails?,
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
    Button(
        onClick = onClick,
        enabled = enabled && !isPurchasing,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CashaPrimaryLight)
    ) {
        if (isPurchasing) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
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

@Composable
fun FooterLinks(
    onRestore: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.subscription_link_restore),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onRestore() }
            )
            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.subscription_link_terms),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onTerms() }
            )
            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.subscription_link_privacy),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onPrivacy() }
            )
        }
        
        Text(
            text = stringResource(R.string.subscription_footer_cancel_anytime),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontSize = 10.sp
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
            CircularProgressIndicator(color = Color.White)
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
                    tint = Color.Green,
                    modifier = Modifier.size(60.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.subscription_success_welcome),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.subscription_success_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun getActionButtonLabel(isPurchasing: Boolean, selectedProduct: com.android.billingclient.api.ProductDetails?): String {
    if (isPurchasing) return stringResource(R.string.subscription_button_processing)
    return when {
        selectedProduct == null -> stringResource(R.string.subscription_button_select_plan)
        selectedProduct.productId.contains("lifetime") -> stringResource(R.string.subscription_button_lifetime)
        else -> stringResource(R.string.subscription_button_start_trial)
    }
}


