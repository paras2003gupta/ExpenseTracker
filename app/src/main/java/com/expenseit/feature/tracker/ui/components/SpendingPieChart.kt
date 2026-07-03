package com.expenseit.feature.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun SpendingPieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit = {}
) {
    val total = slices.map { it.value }.sum()
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (total > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sizeMin = minOf(size.width, size.height)
                val strokeWidth = sizeMin * 0.18f
                val radius = (sizeMin - strokeWidth) / 2f
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset(
                    (size.width - radius * 2) / 2f,
                    (size.height - radius * 2) / 2f
                )

                var startAngle = -90f
                for (slice in slices) {
                    val sweepAngle = (slice.value / total) * 360f
                    if (sweepAngle > 0) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += sweepAngle
                    }
                }
            }
        } else {
            // Draw empty ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sizeMin = minOf(size.width, size.height)
                val strokeWidth = sizeMin * 0.18f
                val radius = (sizeMin - strokeWidth) / 2f
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset(
                    (size.width - radius * 2) / 2f,
                    (size.height - radius * 2) / 2f
                )
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
        centerContent()
    }
}
