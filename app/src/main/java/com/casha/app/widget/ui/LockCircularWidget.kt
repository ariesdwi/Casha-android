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
import com.casha.app.widget.data.*

/**
 * Lock screen circular widget showing budget percentage (Android 14+).
 */
class LockCircularWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            LockCircularContent(context)
        }
    }
}

class LockCircularWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LockCircularWidget()
}

@Composable
private fun LockCircularContent(context: Context) {
    val (state, summary) = resolveWidgetState(context)

    Box(
        modifier = GlanceModifier
            .size(52.dp)
            .cornerRadius(26.dp)
            .background(Color(0xFFF5F5F5))
            .clickable(
                actionRunCallback<DeepLinkAction>(
                    actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://budget")
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            WidgetState.LOGGED_OUT -> Text("👤", style = TextStyle(fontSize = 18.sp))
            WidgetState.NOT_PREMIUM -> Text("👑", style = TextStyle(fontSize = 18.sp))
            WidgetState.NO_DATA -> Text("🔄", style = TextStyle(fontSize = 18.sp))
            WidgetState.HIDDEN -> Text("🔒", style = TextStyle(fontSize = 18.sp))
            WidgetState.NORMAL -> {
                val s = summary!!
                val statusColor = s.spendStatus.color
                // Outer colored ring
                Box(
                    modifier = GlanceModifier
                        .size(52.dp)
                        .cornerRadius(26.dp)
                        .background(statusColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner white circle
                    Box(
                        modifier = GlanceModifier
                            .size(40.dp)
                            .cornerRadius(20.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "BUDGET",
                                style = TextStyle(fontSize = 5.sp, fontWeight = FontWeight.Medium, color = ColorProvider(Color(0xFF666666)))
                            )
                            Text(
                                text = "${s.budgetPctUsed}%",
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(statusColor))
                            )
                        }
                    }
                }
            }
        }
    }
}
