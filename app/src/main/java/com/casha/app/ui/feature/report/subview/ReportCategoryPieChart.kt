package com.casha.app.ui.feature.report.subview

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.casha.app.domain.model.ChartCategorySpending
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ReportCategoryPieChart(
    data: List<ChartCategorySpending>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val totalSpending = data.sumOf { it.total }
    
    // Distinct colors mirroring iOS selection
    val categoryColors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFFF44336), // Red
        Color(0xFF9C27B0), // Purple
        Color(0xFFE91E63), // Pink
        Color(0xFF009688), // Teal
        Color(0xFF3F51B5), // Indigo
        Color(0xFF795548), // Brown
        Color(0xFF00BCD4), // Cyan
        Color(0xFF00FFCC), // Mint-ish
        Color(0xFFFFEB3B)  // Yellow
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chart Area
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val strokeWidth = size.width * 0.22f // Optimized ring width
                    val arcSize = size.width - strokeWidth
                    val arcTopLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    
                    data.forEachIndexed { index, item ->
                        val sweepAngle = (item.total / totalSpending * 360).toFloat()
                        val sliceColor = categoryColors[index % categoryColors.size]
                        
                        drawArc(
                            color = sliceColor,
                            startAngle = startAngle + 1.5f,
                            sweepAngle = sweepAngle - 3f,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        
                        // Draw percentage text overlay if slice is large enough
                        if (item.percentage >= 0.05) {
                            val middleAngle = (startAngle + sweepAngle / 2) * (Math.PI / 180).toFloat()
                            val radius = arcSize / 2 // Position text exactly in the middle of the stroke
                            val x = (size.width / 2) + cos(middleAngle) * radius
                            val y = (size.height / 2) + sin(middleAngle) * radius
                            
                            val percentageText = "${(item.percentage * 100).roundToInt()}%"
                            
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    this.color = android.graphics.Color.WHITE
                                    textSize = 32f // Slightly larger for readability
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    setShadowLayer(5f, 0f, 2f, android.graphics.Color.argb(76, 0, 0, 0))
                                }
                                // Center vertically using FontMetrics
                                val verticalOffset = (paint.descent() + paint.ascent()) / 2
                                drawText(percentageText, x, y - verticalOffset, paint)
                            }
                        }
                        
                        startAngle += sweepAngle
                    }
                }
            }

            // Legend Area (Bottom)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                data.forEachIndexed { index, item ->
                    LegendItem(
                        color = categoryColors[index % categoryColors.count()],
                        label = item.category
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxItemsInEachRow = maxItemsInEachRow,
        content = { content() }
    )
}
