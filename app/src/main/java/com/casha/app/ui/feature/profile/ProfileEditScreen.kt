package com.casha.app.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casha.app.domain.model.UpdateProfileRequest
import com.casha.app.ui.feature.liability.forminput.CashaFormTextField
import com.casha.app.ui.feature.liability.forminput.InputCard
import com.casha.app.ui.theme.CashaDanger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Success handling
    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            viewModel.resetUpdateSuccess()
            onNavigateBack()
        }
    }

    // Form State
    var name by remember(uiState.profile) { mutableStateOf(uiState.profile?.name ?: "") }
    var email by remember(uiState.profile) { mutableStateOf(uiState.profile?.email ?: "") }
    var phone by remember(uiState.profile) { mutableStateOf(uiState.profile?.phone ?: "") }
    
    val isFormValid = name.isNotBlank() && email.isNotBlank() && isValidEmail(email)
    
    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF8F9FA)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxHeight(0.9f),
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA))
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Ubah Profil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = {
                                viewModel.updateProfile(
                                    UpdateProfileRequest(
                                        name = name,
                                        email = email,
                                        phone = phone.ifBlank { null }
                                    )
                                )
                            },
                            enabled = isFormValid
                        ) {
                            Text(
                                "Simpan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isFormValid) Color(0xFF009033) else Color.Gray
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFFF8F9FA)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.errorMessage != null) {
                    Surface(
                        color = CashaDanger.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = CashaDanger,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                InputCard(title = "Informasi Pribadi") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Nama",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CashaFormTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Masukkan nama Anda"
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Email",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CashaFormTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Masukkan email Anda",
                            keyboardType = KeyboardType.Email
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Nomor Telepon",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CashaFormTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "Masukkan nomor telepon (opsional)",
                            keyboardType = KeyboardType.Phone
                        )
                    }
                }
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
    return email.matches(Regex(emailRegEx))
}
