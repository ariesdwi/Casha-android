package com.casha.app.ui.feature.transaction.coordinator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.navigation.NavRoutes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionCoordinator(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AddTransactionCoordinatorViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val encodedUri = URLEncoder.encode(it.toString(), StandardCharsets.UTF_8.toString())
            onNavigate(NavRoutes.Chat.createRoute(encodedUri))
            onDismiss()
            viewModel.close()
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(isPresented) {
        if (isPresented && uiState.presentationState == PresentationState.IDLE) {
            viewModel.showActionSheet()
        } else if (!isPresented) {
            viewModel.close()
        }
    }

    LaunchedEffect(uiState.presentationState) {
        when (uiState.presentationState) {
            PresentationState.MANUAL_ENTRY -> {
                onNavigate(NavRoutes.AddTransaction.route)
                onDismiss()
                viewModel.close()
            }
            PresentationState.CHAT -> {
                onNavigate(NavRoutes.Chat.route)
                onDismiss()
                viewModel.close()
            }
            PresentationState.CAMERA -> {
                onNavigate(NavRoutes.ReceiptCamera.route)
                onDismiss()
                viewModel.close()
            }
            PresentationState.PHOTO_LIBRARY -> {
                imagePickerLauncher.launch("image/*")
                // Don't close/dismiss yet, wait for picker result
            }
            PresentationState.UPGRADE_PROMPT -> {
                onNavigate(NavRoutes.Subscription.route)
                onDismiss()
                viewModel.close()
            }
            PresentationState.IDLE -> {
                onDismiss() // Ensure parent state syncs when ViewModel closes itself
            }
            else -> {}
        }
    }

    if (uiState.presentationState == PresentationState.ACTION_SHEET) {
        Dialog(onDismissRequest = { 
            viewModel.close()
            onDismiss()
        }) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Choose how you'd like to\nadd a transaction",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    CoordinatorOption(
                        title = "Manual Entry",
                        onClick = { viewModel.onFeatureSelected(PresentationState.MANUAL_ENTRY) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CoordinatorOption(
                        title = "Chat with AI",
                        onClick = { viewModel.onFeatureSelected(PresentationState.CHAT) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CoordinatorOption(
                        title = "Take Photo",
                        onClick = { viewModel.onFeatureSelected(PresentationState.CAMERA) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CoordinatorOption(
                        title = "Photo Library",
                        onClick = { viewModel.onFeatureSelected(PresentationState.PHOTO_LIBRARY) }
                    )
                }
            }
        }
    }
}

@Composable
fun CoordinatorOption(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
