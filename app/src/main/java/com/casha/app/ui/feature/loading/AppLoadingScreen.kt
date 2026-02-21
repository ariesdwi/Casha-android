package com.casha.app.ui.feature.loading

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.casha.app.R
import kotlinx.coroutines.delay

@Composable
fun AppLoadingScreen(
    onLoadingComplete: (AppLoadingDestination) -> Unit,
    viewModel: AppLoadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Dot animation logic
    var dotCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }

    // Navigation trigger
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            uiState.destination?.let { onLoadingComplete(it) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.cashalogo),
                contentDescription = "Casha Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Indicator
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading Text with Animated Dots
            Text(
                text = "Syncing all data" + ".".repeat(dotCount),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            // Error Message & Retry
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Oops! $error",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.retry() }) {
                    Text("Retry")
                }
            }
        }
    }
}
