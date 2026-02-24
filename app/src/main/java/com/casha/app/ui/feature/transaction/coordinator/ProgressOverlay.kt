package com.casha.app.ui.feature.transaction.coordinator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ProgressOverlay(progressState: ProgressState) {
    if (!progressState.isVisible) return

    Dialog(
        onDismissRequest = { /* Prevent dismiss while processing */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false // Fills screen
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // "UltraThinMaterial" frosted glass background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .blur(20.dp)
            )

            // Card Container
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 30.dp,
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .widthIn(min = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    LoadingSpinner(progressState)
                    StatusContent(progressState)
                }
            }
        }
    }
}

@Composable
private fun LoadingSpinner(state: ProgressState) {
    val primaryColor = Color(0xFF00C896) // CashaPrimary
    val accentColor = Color(0xFF0D9488)  // CashaAccent

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val scale by animateFloatAsState(
        targetValue = if (state.isCompleted) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
        // Glowing Background
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.15f),
                        accentColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension / 2
                )
            )
        }

        // Static Outer Ring
        Canvas(modifier = Modifier.size(80.dp)) {
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        accentColor.copy(alpha = 0.2f),
                        primaryColor.copy(alpha = 0.1f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // Animated Trimmed Ring (Spinning Sweep)
        if (!state.isCompleted) {
            Canvas(
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotation)
            ) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor, accentColor)
                    ),
                    startAngle = 0f,
                    sweepAngle = 250f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        // Central Icon Pulse
        Icon(
            imageVector = state.currentIcon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier
                .size(32.dp)
                .scale(scale)
        )
    }
}

@Composable
private fun StatusContent(state: ProgressState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Text
        AnimatedContent(
            targetState = state.currentProcessingStatus,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.8f)).togetherWith(fadeOut())
            }
        ) { text ->
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Progress Bar & Percentage
        if (!state.isCompleted) {
            ProgressBar(state.processingProgress)
            ProgressDetails(state)
        }
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    val primaryColor = Color(0xFF00C896)
    val accentColor = Color(0xFF0D9488)

    // Animate progress gracefully
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(primaryColor, accentColor)))
        )
    }
}

@Composable
private fun ProgressDetails(state: ProgressState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${(state.processingProgress * 100).toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AnimatedDots()
    }
}

@Composable
private fun AnimatedDots() {
    val primaryColor = Color(0xFF00C896)
    val accentColor = Color(0xFF0D9488)

    val infiniteTransition = rememberInfiniteTransition()

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 0 until 3) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, delayMillis = i * 200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, delayMillis = i * 200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(primaryColor, accentColor)))
            )
        }
    }
}
