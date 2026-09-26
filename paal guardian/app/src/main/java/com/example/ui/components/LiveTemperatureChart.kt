package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun LiveTemperatureChart(
    temperatures: List<Double>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Temperature Telemetry Chart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("4-8°C Safe", fontSize = 10.sp, color = Color(0xFF5A665E))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFD32F2F), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(">8°C Warning", fontSize = 10.sp, color = Color(0xFFD32F2F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (temperatures.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awaiting live temperature data stream...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B968F)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Chart Y limits: Min = 3.0°C, Max = 11.0°C
                        val minTemp = 3.0f
                        val maxTemp = 11.0f
                        val range = maxTemp - minTemp

                        fun tempToY(temp: Double): Float {
                            val normalized = (temp.toFloat() - minTemp) / range
                            return height - (normalized * height).coerceIn(0f, height)
                        }

                        // Draw shaded green safe zone (4°C to 8°C)
                        val y4 = tempToY(4.0)
                        val y8 = tempToY(8.0)
                        val safeZoneTop = y8
                        val safeZoneHeight = y4 - y8
                        drawRect(
                            color = Color(0xFFE8F5E9),
                            topLeft = Offset(0f, safeZoneTop),
                            size = Size(width, safeZoneHeight)
                        )

                        // Draw dashed line for 8°C warning threshold
                        drawLine(
                            color = Color(0xFFFFCDD2),
                            start = Offset(0f, y8),
                            end = Offset(width, y8),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )

                        // Draw dashed line for 4°C lower boundary
                        drawLine(
                            color = Color(0xFFC8E6C9),
                            start = Offset(0f, y4),
                            end = Offset(width, y4),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )

                        // Plot temperature curve
                        val stepX = if (temperatures.size > 1) {
                            width / (temperatures.size - 1).coerceAtLeast(1)
                        } else {
                            width
                        }

                        val linePath = Path()
                        val points = mutableListOf<Offset>()

                        for (i in temperatures.indices) {
                            val x = i * stepX
                            val y = tempToY(temperatures[i])
                            val point = Offset(x, y)
                            points.add(point)

                            if (i == 0) {
                                linePath.moveTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                            }
                        }

                        // Draw main line path
                        drawPath(
                            path = linePath,
                            color = Color(0xFF1B5E20),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw circular points
                        for (i in points.indices) {
                            val pt = points[i]
                            val temp = temperatures[i]
                            val pointColor = if (temp > 8.0) Color(0xFFD32F2F) else Color(0xFF2E7D32)

                            // Outer border
                            drawCircle(
                                color = Color.White,
                                radius = 5.dp.toPx(),
                                center = pt
                            )
                            // Inner colored dot
                            drawCircle(
                                color = pointColor,
                                radius = 3.5.dp.toPx(),
                                center = pt
                            )
                        }

                        // Highlight the latest reading
                        if (points.isNotEmpty()) {
                            val latest = points.last()
                            val latestTemp = temperatures.last()
                            val latestColor = if (latestTemp > 8.0) Color(0xFFD32F2F) else Color(0xFF2E7D32)

                            drawCircle(
                                color = latestColor.copy(alpha = 0.3f),
                                radius = 10.dp.toPx(),
                                center = latest
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Start", fontSize = 11.sp, color = Color(0xFF8B968F))
                    Text("Cold-Chain Safe Band (4.0°C - 8.0°C)", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    Text("Latest: ${String.format(Locale.US, "%.1f", temperatures.lastOrNull() ?: 0.0)}°C", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if ((temperatures.lastOrNull() ?: 0.0) > 8.0) Color(0xFFD32F2F) else Color(0xFF2E7D32))
                }
            }
        }
    }
}
