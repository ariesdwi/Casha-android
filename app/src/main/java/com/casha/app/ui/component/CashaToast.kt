package com.casha.app.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.core.util.ToastEvent
import kotlinx.coroutines.delay

private data class ToastStyle(
    val icon: ImageVector,
    val iconColor: Color,
    val iconBg: Color,
    val borderColor: Color
)

@Composable
private fun toastStyle(event: ToastEvent): ToastStyle {
    val success = MaterialTheme.colorScheme.primary
    val error   = MaterialTheme.colorScheme.error
    val warning = Color(0xFFF57C00)
    val info    = Color(0xFF2196F3)

    return when (event) {
        is ToastEvent.Success -> ToastStyle(
            icon = Icons.Default.CheckCircle,
            iconColor = success,
            iconBg = success.copy(alpha = 0.12f),
            borderColor = success.copy(alpha = 0.25f)
        )
        is ToastEvent.Error -> ToastStyle(
            icon = Icons.Default.Error,
            iconColor = error,
            iconBg = error.copy(alpha = 0.12f),
            borderColor = error.copy(alpha = 0.25f)
        )
        is ToastEvent.Warning -> ToastStyle(
            icon = Icons.Default.Warning,
            iconColor = warning,
            iconBg = warning.copy(alpha = 0.12f),
            borderColor = warning.copy(alpha = 0.25f)
        )
        is ToastEvent.Info -> ToastStyle(
            icon = Icons.Default.Info,
            iconColor = info,
            iconBg = info.copy(alpha = 0.12f),
            borderColor = info.copy(alpha = 0.25f)
        )
    }
}

/**
 * Displays a single animated toast above the tab bar.
 *
 * Place this inside the outermost [Box] in MainScreen, aligned to BottomCenter,
 * with padding that accounts for the tab bar height.
 *
 * @param toastEvent  The current toast event to show, or null to hide.
 * @param onDismiss   Called after the toast auto-dismisses or when overridden.
 * @param modifier    Modifier applied to the outer animated container.
 * @param durationMs  How long the toast is visible before auto-dismiss (default 3 s).
 */
@Composable
fun CashaToastHost(
    toastEvent: ToastEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMs: Long = 3000L
) {
    // Keep a "last seen" toast so the animation can complete before null clears it
    var visibleEvent by remember { mutableStateOf<ToastEvent?>(null) }
    var show by remember { mutableStateOf(false) }

    LaunchedEffect(toastEvent) {
        if (toastEvent != null) {
            visibleEvent = toastEvent
            show = true
            delay(durationMs)
            show = false
            delay(350) // wait for exit animation
            onDismiss()
        } else {
            show = false
        }
    }

    AnimatedVisibility(
        visible = show,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = tween(320),
            initialOffsetY = { it + 40 }
        ) + fadeIn(animationSpec = tween(250)),
        exit = slideOutVertically(
            animationSpec = tween(280),
            targetOffsetY = { it + 40 }
        ) + fadeOut(animationSpec = tween(200))
    ) {
        visibleEvent?.let { event ->
            CashaToastCard(event = event)
        }
    }
}

@Composable
private fun CashaToastCard(event: ToastEvent) {
    val style = toastStyle(event)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Colored icon badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(style.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
