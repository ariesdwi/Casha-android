package com.casha.app.ui.feature.transaction.voice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.domain.model.ChatParseResult

// ── Supported languages ───────────────────────────────────────────────────────

data class SupportedLocale(val flag: String, val displayName: String, val localeId: String)

val supportedLocales = listOf(
    SupportedLocale("🇺🇸", "English",    "en-US"),
    SupportedLocale("🇮🇩", "Indonesia",  "id-ID"),
    SupportedLocale("🇸🇦", "العربية",    "ar-SA"),
    SupportedLocale("🇩🇪", "Deutsch",    "de-DE"),
    SupportedLocale("🇪🇸", "Español",    "es-ES"),
    SupportedLocale("🇫🇷", "Français",   "fr-FR"),
    SupportedLocale("🇮🇳", "हिन्दी",     "hi-IN"),
    SupportedLocale("🇯🇵", "日本語",      "ja-JP"),
    SupportedLocale("🇰🇷", "한국어",      "ko-KR"),
    SupportedLocale("🇧🇷", "Português",  "pt-BR"),
    SupportedLocale("🇨🇳", "中文",       "zh-CN"),
)

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTransactionSheet(
    onDismiss: () -> Unit,
    onSuccess: (ChatParseResult) -> Unit,
    viewModel: VoiceRecorderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val parseSuccess by viewModel.parseSuccess.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    // ── Auto-send when STOPPED ──────────────────────────────────────────────
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == VoicePhase.STOPPED) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.sendTranscriptToAI()
        } else if (uiState.phase == VoicePhase.ERROR) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // ── Consume success result and close ────────────────────────────────────
    LaunchedEffect(parseSuccess) {
        parseSuccess?.let { result ->
            viewModel.clearParseSuccess()
            onSuccess(result)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.reset()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ──────────────────────────────────────────────────────
            VoiceSheetHeader(
                selectedLocaleId = uiState.selectedLocaleId,
                isRecording = uiState.phase == VoicePhase.RECORDING,
                onLocaleSelected = { viewModel.switchLocale(it) },
                onClose = {
                    viewModel.reset()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Body – switches on phase ─────────────────────────────────────
            when (uiState.phase) {
                VoicePhase.PERMISSION_REQUEST -> PermissionRequestView()
                VoicePhase.PERMISSION_DENIED  -> PermissionDeniedView()
                VoicePhase.IDLE               -> IdleView(onStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.requestPermissions()
                })
                VoicePhase.RECORDING          -> RecordingView(
                    transcript = uiState.transcript,
                    onStop = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.stopRecording() 
                    },
                    onCancel = { viewModel.reset() }
                )
                VoicePhase.STOPPED,
                VoicePhase.SENDING            -> SendingView(transcript = uiState.transcript)
                VoicePhase.ERROR              -> ErrorView(
                    message = uiState.errorMessage ?: "Something went wrong.",
                    onRetry = { viewModel.reset() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Permission launcher (shown outside bottom-sheet) ─────────────────────
    MicPermissionHandler(viewModel = viewModel, uiState = uiState)
}

// ── Permission handler ────────────────────────────────────────────────────────

@Composable
private fun MicPermissionHandler(
    viewModel: VoiceRecorderViewModel,
    uiState: VoiceUiState
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionsResult(granted)
        if (granted) viewModel.startRecording()
    }

    // When IDLE mic tapped, we request permission here via effect driven by a shared event.
    // The IdleView calls a callback stored in the ViewModel via a lambda passed down.
    // We expose the launcher via a side-effect: composable re-emits on phase == PERMISSION_REQUEST.
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == VoicePhase.PERMISSION_REQUEST) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun VoiceSheetHeader(
    selectedLocaleId: String,
    isRecording: Boolean,
    onLocaleSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    var showLocalePicker by remember { mutableStateOf(false) }
    val currentLocale = supportedLocales.find { it.localeId == selectedLocaleId } ?: supportedLocales[0]

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mic icon badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Voice Transaction",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Language picker chip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable(
                enabled = !isRecording,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { showLocalePicker = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = currentLocale.flag, fontSize = 14.sp)
                Text(
                    text = currentLocale.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isRecording) MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Close button
        IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // ── Locale picker dropdown ───────────────────────────────────────────────
    if (showLocalePicker) {
        LocalePickerDialog(
            selectedLocaleId = selectedLocaleId,
            onLocaleSelected = { locale ->
                onLocaleSelected(locale.localeId)
                showLocalePicker = false
            },
            onDismiss = { showLocalePicker = false }
        )
    }
}

// ── Locale picker dialog ──────────────────────────────────────────────────────

@Composable
private fun LocalePickerDialog(
    selectedLocaleId: String,
    onLocaleSelected: (SupportedLocale) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                supportedLocales.forEach { locale ->
                    val isSelected = locale.localeId == selectedLocaleId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onLocaleSelected(locale) }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else Color.Transparent
                            )
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = locale.flag, fontSize = 20.sp)
                        Text(
                            text = locale.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Phase views ───────────────────────────────────────────────────────────────

@Composable
private fun PermissionRequestView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Requesting permissions…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionDeniedView() {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.MicOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Microphone & Speech access required",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please enable Microphone access in your device settings to use Voice Note.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Open Settings")
        }
    }
}

@Composable
private fun IdleView(onStart: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Tap to start recording",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Large mic button
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Start recording",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "e.g. \"Spent 50k on lunch\"",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun RecordingView(
    transcript: String,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.20f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    val recordingRed = Color(0xFFE53935)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Listening indicator
        Text(
            text = "Listening…",
            style = MaterialTheme.typography.labelMedium,
            color = recordingRed,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Pulsing stop button
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(ring2Scale)
                    .clip(CircleShape)
                    .background(recordingRed.copy(alpha = ring2Alpha))
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(ring1Scale)
                    .clip(CircleShape)
                    .background(recordingRed.copy(alpha = ring1Alpha))
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(recordingRed)
                    .clickable(onClick = onStop),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop recording",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Live transcript
        if (transcript.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = transcript,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Cancel link
        TextButton(onClick = onCancel) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SendingView(transcript: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Processing your transaction…",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        if (transcript.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"$transcript\"",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to record again",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = onRetry)
        )
    }
}
