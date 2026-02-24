package com.casha.app.ui.feature.transaction.coordinator

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.domain.model.ChatParseIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMessageScreen(
    initialImageUri: Uri? = null,
    onClose: () -> Unit,
    viewModel: AddMessageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showSourceSelection by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Persist the read permission so the ViewModel can read it later
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if provider doesn't support persistable permissions
            }
            viewModel.sendImage(it) 
        }
    }

    // Clear any previous state when opening the screen
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    // Process initial image if passed from coordinator
    LaunchedEffect(initialImageUri) {
        initialImageUri?.let {
            viewModel.sendImage(it)
        }
    }

    // Auto-scroll when messages update
    LaunchedEffect(uiState.sentMessages, uiState.isSending, uiState.showConfirmation) {
        delay(100) // Small delay to allow UI to render new items
        val lastIndex = if (uiState.showConfirmation) {
            uiState.sentMessages.size + 2 // Welcome + Messages + Confirmation
        } else if (uiState.isSending) {
            uiState.sentMessages.size + 2 // Welcome + Messages + Processing
        } else {
            uiState.sentMessages.size + 1 // Welcome + Messages
        }
        if (lastIndex > 0) {
            listState.animateScrollToItem(lastIndex - 1)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Green Chat Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = Color(0xFF00C896),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "New Transaction",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                        Text(
                            text = "Keep it short and simple",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF888AAA)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onClose()
                        },
                        enabled = !uiState.isSending,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF0F0F0), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Close", 
                            tint = Color(0xFF888AAA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Column {
                    // Transparent-to-white gradient to match the screenshot's floating effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEEEEF8))
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Green Camera Icon
                        IconButton(
                            onClick = { showSourceSelection = true },
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFE8F5E9), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, 
                                contentDescription = "Camera", 
                                tint = Color(0xFF00C896),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Pill-shaped TextField
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFF5F6F7),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Simplified multi-line text input
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (messageInput.isEmpty()) {
                                        Text(
                                            text = "Describe your transaction...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF888AAA).copy(alpha = 0.6f)
                                        )
                                    }
                                    
                                    // Use a basic text field for better control
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = messageInput,
                                        onValueChange = { messageInput = it },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1A1A2E)),
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (messageInput.isNotBlank()) {
                                                    viewModel.sendMessage(messageInput)
                                                    messageInput = ""
                                                    focusManager.clearFocus()
                                                }
                                            }
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Grey Send Icon
                        IconButton(
                            onClick = {
                                if (messageInput.isNotBlank()) {
                                    viewModel.sendMessage(messageInput)
                                    messageInput = ""
                                    focusManager.clearFocus()
                                }
                            },
                            enabled = messageInput.isNotBlank(),
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFF0F0F0), CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send, 
                                contentDescription = "Send", 
                                tint = if (messageInput.isNotBlank()) Color(0xFF888AAA) else Color(0xFFAAAAAA).copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Extra padding for navigation bar area
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeMessageView(
                    onExampleClick = { messageInput = it },
                    onCameraClick = { showSourceSelection = true }
                )
            }
            
            items(uiState.sentMessages) { msg ->
                UserMessageView(message = msg)
            }
            
            if (uiState.isSending) {
                item {
                    ProcessingMessageView()
                }
            } else if (uiState.showConfirmation) {
                item {
                    ConfirmationMessageView(
                        isSuccess = uiState.transactionSuccess,
                        message = uiState.aiResponseMessage,
                        intent = uiState.lastIntent
                    )
                }
            }
        }

        if (showSourceSelection) {
            AlertDialog(
                onDismissRequest = { showSourceSelection = false },
                title = { Text("Select Source") },
                text = {
                    Column {
                        ListItem(
                            headlineContent = { Text("Photo Library") },
                            leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                            modifier = Modifier.clickable {
                                showSourceSelection = false
                                imagePickerLauncher.launch("image/*")
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Cancel") },
                            leadingContent = { Icon(Icons.Default.Cancel, null) },
                            modifier = Modifier.clickable { showSourceSelection = false }
                        )
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun WelcomeMessageView(
    onExampleClick: (String) -> Unit,
    onCameraClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFFF5F6F7),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Icon(
                    Icons.Default.AutoAwesome, 
                    contentDescription = null, 
                    tint = Color(0xFF00C896),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Hi there! 👋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "How to use",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF00C896),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF00C896),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Track your finances naturally! Tell me about your expenses, income, or payments.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF888AAA),
                lineHeight = 22.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFEEEEF8))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "TRY THESE EXAMPLES", 
                        style = MaterialTheme.typography.labelSmall, 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF888AAA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExampleRow(Icons.Default.ShoppingCart, "50k coffee this morning", "Expense", Color(0xFFFF6B6B)) {
                        onExampleClick("50k coffee this morning")
                    }
                    ExampleRow(Icons.Default.ArrowDownward, "Received 5m salary today", "Income", Color(0xFF00C896)) {
                        onExampleClick("Received 5m salary today")
                    }
                    ExampleRow(Icons.Default.CreditCard, "Paid 2m credit card", "Payment", Color(0xFF6C63FF)) {
                        onExampleClick("Paid 2m credit card")
                    }
                    ExampleRow(Icons.Default.PhotoCamera, "Upload a receipt photo", null, null) {
                        onCameraClick()
                    }
                }
            }
        }
    }
}

@Composable
fun ExampleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    text: String, 
    badgeText: String?, 
    badgeColor: Color?,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.width(8.dp))
        
        if (badgeText != null && badgeColor != null) {
            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun UserMessageView(message: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(horizontalAlignment = Alignment.End) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp
                ),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("You • Sent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProcessingMessageView() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Processing", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating your transaction...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ConfirmationMessageView(isSuccess: Boolean, message: String, intent: String) {
    val color = if (isSuccess) Color(0xFF00C896) else Color(0xFFFF6B6B)
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning
    
    val badgeColor = when (intent) {
        ChatParseIntent.EXPENSE.rawValue -> Color(0xFFFF6B6B)
        ChatParseIntent.INCOME.rawValue -> Color(0xFF00C896)
        ChatParseIntent.PAYMENT.rawValue -> Color(0xFF6C63FF)
        else -> Color(0xFF888AAA)
    }
    
    Surface(
        color = Color(0xFFF5F6F7),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSuccess) "Transaction Logged" else "Something went wrong",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    
                    if (isSuccess && intent.isNotEmpty()) {
                        Surface(
                            color = badgeColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = intent.uppercase(),
                                color = badgeColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888AAA)
                )
            }
        }
    }
}
