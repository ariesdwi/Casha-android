package com.casha.app.ui.feature.transaction.coordinator

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload

data class ProgressState(
    val isVisible: Boolean = false,
    val currentProcessingStatus: String = "Uploading Receipt...",
    val currentIcon: ImageVector = Icons.Default.CloudUpload,
    val processingProgress: Float = 0f,
    val isCompleted: Boolean = false
) {
    val isInProgress: Boolean
        get() = isVisible && !isCompleted
}
