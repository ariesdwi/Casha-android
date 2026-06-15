package com.casha.app.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.casha.app.widget.WidgetTheme
import com.casha.app.widget.data.*
import com.casha.app.widget.util.WidgetLogger

/**
 * Lock screen circular widget showing budget percentage (Android 14+).
 * 
 * Modern Design Features:
 * - Larger text (12sp percentage, was 11sp)
 * - Better visual hierarchy
 * - Gradient-like appearance with color rings
 * - Material Design 3 styling
 * - WidgetTheme integration
 */
class LockCircularWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetLogger.logRender("LockCircularWidget", "provideGlance")
            LockCircularContent(context)
        }
    }
}

class LockCircularWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LockCircularWidget()
}

/**
 * Circular widget content with modern design.
 * 
 * Improvements:
 * - Uses WidgetTheme colors
 * - Larger text (12sp percentage)
 * - Better visual with stronger color
 * - Logging integrated
 */
@Composable
private fun LockCircularContent(context: Context) {
    val (state, summary) = resolveWidgetState(context)
    
    WidgetLogger.logRender("LockCircularWidget", state.name)

    Box(
        modifier = GlanceModifier
            .size(WidgetTheme.LockCircularSize) // 52dp
            .cornerRadius(WidgetTheme.LockCircularSize / 2) // Fully rounded
            .background(WidgetTheme.BackgroundLight) // Light gray background
            .clickable(
                actionRunCallback<DeepLinkAction>(
                    actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://budget")
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            WidgetState.LOGGED_OUT -> Text("👤", style = TextStyle(fontSize = 20.sp)) // Larger emoji
            WidgetState.NOT_PREMIUM -> Text("👑", style = TextStyle(fontSize = 20.sp))
            WidgetState.NO_DATA -> Text("🔄", style = TextStyle(fontSize = 20.sp))
            WidgetState.HIDDEN -> Text("🔒", style = TextStyle(fontSize = 20.sp))
            WidgetState.NORMAL -> {
                val s = summary!!
                val statusColor = WidgetTheme.getStatusColor(s.spendStatus)
                
                // Outer colored ring
                Box(
                    modifier = GlanceModifier
                        .size(WidgetTheme.LockCircularSize) // 52dp
                        .cornerRadius(WidgetTheme.LockCircularSize / 2)
                        .background(statusColor.copy(alpha = 0.25f)), // Slightly more visible (was 0.25f)
                    contentAlignment = Alignment.Center
                ) {
                    // Inner white circle
                    Box(
                        modifier = GlanceModifier
                            .size(WidgetTheme.LockCircularInnerSize) // 40dp
                            .cornerRadius(WidgetTheme.LockCircularInnerSize / 2)
                            .background(WidgetTheme.BackgroundCard), // White
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "BUDGET",
                                style = TextStyle(
                                    fontSize = 6.sp, // Slightly larger (was 5sp)
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(WidgetTheme.TextTertiary)
                                )
                            )
                            Text(
                                text = "${s.budgetPctUsed}%",
                                style = TextStyle(
                                    fontSize = 12.sp, // Larger (was 11sp)
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(statusColor)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
