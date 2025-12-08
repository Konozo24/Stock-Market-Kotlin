package com.example.brokerx.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.integrity.p

@Composable
fun LineChart(
    prices: List<Float>,
    times: List<String>,
    modifier: Modifier = Modifier,
) {
    if (prices.isEmpty()) return

    val selectedIndex = remember { mutableStateOf<Int?>(null) }
    val pointsState = remember { mutableStateOf<List<Offset>>(emptyList()) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(true) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width
                        val pointSpacing = chartWidth / (prices.size - 1).coerceAtLeast(1)
                        val index = (offset.x / (size.width / (prices.size - 1).coerceAtLeast(1))).toInt()
                            .coerceIn(0, prices.lastIndex)

                        // Toggle tooltip
                        selectedIndex.value = if (selectedIndex.value == index) null else index
                    }
                }
        ) {
            val maxPrice = prices.maxOrNull() ?: return@Canvas
            val minPrice = prices.minOrNull() ?: return@Canvas
            val priceRange = maxPrice - minPrice

            val points = prices.mapIndexed { index, price ->
                val x = index * (size.width / (prices.size - 1).coerceAtLeast(1))
                val yRatio = if (priceRange == 0f) 0.5f else (price - minPrice) / priceRange
                val y = size.height - (yRatio * size.height)
                Offset(x, y)
            }
            pointsState.value = points // store for tooltip

            // Draw the line
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val current = points[i]
                    val midPoint = Offset(
                        (prev.x + current.x) / 2,
                        (prev.y + current.y) / 2
                    )
                    quadraticBezierTo(prev.x, prev.y, midPoint.x, midPoint.y)
                }
            }

            // Determine bullish/bearish
            val isBullish = if (prices.size >= 2) {
                prices.last() > prices[prices.size - 2]
            } else {
                false
            }
            val lineColor = if (isBullish) Color(0xFF4CAF50) else Color(0xFFF44336) // green/red

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Vertical marker line on tap
            selectedIndex.value?.let { i ->
                val p = points[i]
                drawLine(
                    color = Color.Gray,
                    start = Offset(p.x, 0f),
                    end = Offset(p.x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Tooltip overlay
        selectedIndex.value?.let { i ->
            val date = if (times.isNotEmpty() && i < times.size) times[i] else "N/A"
            val price = prices[i]
            val points = pointsState.value
            if (points.isNotEmpty()) {
                val p = points[i]
                Box(
                    modifier = Modifier
                        .offset(x = with(LocalDensity.current) { p.x.toDp() - 40.dp }, y = 0.dp) // center tooltip
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (date != "N/A") "$date\n$${String.format("%.2f", price)}"
                        else "$${String.format("%.2f", price)}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
