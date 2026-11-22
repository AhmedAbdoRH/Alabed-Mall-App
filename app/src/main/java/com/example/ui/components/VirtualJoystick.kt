package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun VirtualJoystick(
    onMoveTick: (dx: Float, dy: Float, deltaSeconds: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerSize = 122.dp
    val maxRadiusPx = with(LocalDensity.current) { 38.dp.toPx() }
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val latestOnMove by rememberUpdatedState(onMoveTick)

    LaunchedEffect(isDragging) {
        if (isDragging) {
            var lastTimeNanos = 0L
            while (isDragging) {
                withFrameNanos { frameNanos ->
                    if (lastTimeNanos != 0L) {
                        val dt = ((frameNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                        val nx = (knobOffset.x / maxRadiusPx).coerceIn(-1f, 1f)
                        val ny = (knobOffset.y / maxRadiusPx).coerceIn(-1f, 1f)
                        latestOnMove(nx, ny, dt)
                    }
                    lastTimeNanos = frameNanos
                }
            }
        } else {
            latestOnMove(0f, 0f, 0f)
        }
    }

    val goldAccent = MaterialTheme.colorScheme.secondary
    val tealPrimary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.testTag("virtual_joystick"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(outerSize)
                .pointerInput(maxRadiusPx) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            knobOffset = Offset.Zero
                        },
                        onDragCancel = {
                            isDragging = false
                            knobOffset = Offset.Zero
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val raw = knobOffset + dragAmount
                            val dist = hypot(raw.x, raw.y)
                            knobOffset = if (dist > maxRadiusPx) {
                                Offset(raw.x / dist * maxRadiusPx, raw.y / dist * maxRadiusPx)
                            } else {
                                raw
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(outerSize)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = size.minDimension / 2f - 4.dp.toPx()

                // Soft shadow under bezel
                drawCircle(
                    color = Color(0x55000000),
                    radius = outerRadius + 1.dp.toPx(),
                    center = Offset(center.x, center.y + 3.dp.toPx())
                )

                // Dark metallic game bezel
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xE61E293B),
                            Color(0xEE0F172A)
                        ),
                        center = center,
                        radius = outerRadius
                    ),
                    radius = outerRadius,
                    center = center
                )

                // Active neon rim
                drawCircle(
                    color = if (isDragging) Color(0xFF22D3EE) else goldAccent.copy(alpha = 0.72f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = (if (isDragging) 3.dp else 2.2.dp).toPx())
                )

                // Inner calibration ring + N/S/E/W tactical ticks
                drawCircle(
                    color = Color.White.copy(alpha = 0.16f),
                    radius = outerRadius * 0.56f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                for (i in 0 until 4) {
                    val angle = i * PI / 2.0
                    val innerPt = Offset(
                        center.x + (cos(angle) * outerRadius * 0.68f).toFloat(),
                        center.y + (sin(angle) * outerRadius * 0.68f).toFloat()
                    )
                    val outerPt = Offset(
                        center.x + (cos(angle) * outerRadius * 0.88f).toFloat(),
                        center.y + (sin(angle) * outerRadius * 0.88f).toFloat()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.42f),
                        start = innerPt,
                        end = outerPt,
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // 3D-Shaded Analog Thumbstick Knob
                val knobCenter = center + knobOffset
                val knobRadius = 22.dp.toPx()
                drawCircle(
                    color = Color(0x66000000),
                    radius = knobRadius,
                    center = Offset(knobCenter.x, knobCenter.y + 3.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            goldAccent,
                            tealPrimary
                        ),
                        center = Offset(knobCenter.x - 5.dp.toPx(), knobCenter.y - 5.dp.toPx()),
                        radius = knobRadius * 1.4f
                    ),
                    radius = knobRadius,
                    center = knobCenter
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = knobRadius,
                    center = knobCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 7.dp.toPx(),
                    center = knobCenter
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xD90F172A),
            shadowElevation = 3.dp
        ) {
            Text(
                text = "تحريك الشخصية",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFF8FAFC),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
