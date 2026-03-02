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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.navigation.NavRoutes
import androidx.compose.ui.res.stringResource
import com.casha.app.R

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.io.File
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionCoordinator(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AddTransactionCoordinatorViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // ── Photo Library Launcher ──
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Persist the read permission so the ChatScreen can read it later
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if provider doesn't support persistable permissions
            }
            // Pass the URI to the ViewModel to upload directly (Scan & Go flow)
            viewModel.uploadImage(it)
        }
    }

    // ── Camera Launcher ──
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { viewModel.uploadImage(it) }
        } else {
            // User cancelled camera — just close the coordinator
            viewModel.close()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create temp file and launch camera
            val tempFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            android.widget.Toast.makeText(context, "Camera permission is required to take photos", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.close()
        }
    }

    fun launchCamera() {
        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
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
                onNavigate(NavRoutes.Chat.createRoute())
                onDismiss()
                viewModel.close()
            }
            PresentationState.CAMERA -> {
                launchCamera()
                // Don't close/dismiss yet, wait for camera result
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

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // New custom animated ProgressOverlay matching iOS
    ProgressOverlay(progressState = uiState.progressState)
    
    if (uiState.presentationState == PresentationState.ACTION_SHEET && !uiState.progressState.isVisible) {
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
                        text = stringResource(R.string.add_transaction_coordinator_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.add_transaction_coordinator_message_premium),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    CoordinatorOption(
                        title = stringResource(R.string.add_transaction_coordinator_button_manual),
                        onClick = { viewModel.onFeatureSelected(PresentationState.MANUAL_ENTRY) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CoordinatorOption(
                        title = stringResource(R.string.add_transaction_coordinator_button_chat),
                        onClick = { viewModel.onFeatureSelected(PresentationState.CHAT) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CoordinatorOption(
                        title = stringResource(R.string.add_transaction_coordinator_button_camera),
                        onClick = { viewModel.onFeatureSelected(PresentationState.CAMERA) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CoordinatorOption(
                        title = stringResource(R.string.add_transaction_coordinator_button_photo_library),
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

