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
import com.casha.app.widget.util.WidgetCurrencyFormatter

/**
 * Lock screen rectangular widget showing budget bar (Android 14+).
 */
class LockRectangularWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            LockRectangularContent(context)
        }
    }
}

class LockRectangularWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LockRectangularWidget()
}

@Composable
private fun LockRectangularContent(context: Context) {
    val (state, summary) = resolveWidgetState(context)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(12.dp)
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(
                actionRunCallback<DeepLinkAction>(
                    actionParametersOf(DeepLinkAction.DeepLinkKey to "casha://budget")
                )
            )
    ) {
        when (state) {
            WidgetState.LOGGED_OUT -> LockFallback("👤", "Belum login", "Buka Casha untuk mulai")
            WidgetState.NOT_PREMIUM -> LockFallback("👑", "Casha Premium", "Upgrade untuk akses widget")
            WidgetState.NO_DATA -> LockFallback("🔄", "Menunggu data", "Buka app untuk sinkronisasi")
            WidgetState.HIDDEN -> LockFallback("🔒", "Balance hidden", "••••")
            WidgetState.NORMAL -> LockNormalContent(summary!!)
        }
    }
}

@Composable
private fun LockNormalContent(summary: WidgetSummary) {
    val statusColor = summary.spendStatus.color

    Column(modifier = GlanceModifier.fillMaxSize()) {
        // Row 1: "SAFE SPEND TODAY" + status badge
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "SAFE SPEND TODAY",
                style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF666666)))
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Box(
                modifier = GlanceModifier
                    .cornerRadius(4.dp)
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = summary.statusLabel,
                    style = TextStyle(fontSize = 7.sp, fontWeight = FontWeight.Bold, color = ColorProvider(statusColor))
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        // Row 2: Amount
        Text(
            text = WidgetCurrencyFormatter.formatFull(summary.safeSpendToday, summary.currency),
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.Black))
        )

        Spacer(modifier = GlanceModifier.height(3.dp))

        // Row 3: Progress bar
        val progress = if (summary.safeSpendToday > 0) {
            (summary.spentToday / summary.safeSpendToday).coerceIn(0.0, 1.0).toFloat()
        } else 0f
        val barColor = when {
            progress < 0.8f -> Color(0xFF2E7D32)
            progress < 1.0f -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(3.dp)
                .cornerRadius(2.dp)
                .background(Color(0xFFE0E0E0))
        ) {
            Row(modifier = GlanceModifier.fillMaxSize()) {
                if (progress > 0.01f) {
                    Box(
                        modifier = GlanceModifier.height(3.dp).defaultWeight().cornerRadius(2.dp).background(barColor)
                    ) {}
                }
                if (progress < 0.99f) {
                    Spacer(modifier = GlanceModifier.height(3.dp).defaultWeight())
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(2.dp))

        // Row 4: Footer text
        Text(
            text = "${summary.budgetPctUsed}% budget • ${summary.daysRemaining} hari lagi",
            style = TextStyle(fontSize = 8.sp, color = ColorProvider(Color(0xFF888888)))
        )
    }
}

@Composable
private fun LockFallback(icon: String, title: String, subtitle: String) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(text = icon, style = TextStyle(fontSize = 20.sp))
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.Black))
            )
            Text(
                text = subtitle,
                style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color(0xFF888888)))
            )
        }
    }
}
