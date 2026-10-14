package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorView(
    isListening: Boolean,
    isSpeaking: Boolean,
    isWakeWordActive: Boolean,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    // Outer ring rotation
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationOuter"
    )

    // Inner counter-rotation
    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationInner"
    )

    // Pulse animation for listening/speaking
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (isListening || isSpeaking) 1.15f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isListening) 650 else if (isSpeaking) 900 else 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val coreGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isListening || isSpeaking) 0.95f else 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coreGlowAlpha"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // High-tech Arc Reactor Canvas Background
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // Outer subtle energy aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HudCyanGlow.copy(alpha = if (isListening) 0.35f else 0.15f),
                        HudBlueElectric.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // Outer segmented turbine ring (Clockwise rotation)
            rotate(degrees = rotationOuter, pivot = center) {
                val segments = 12
                val segRadius = maxRadius * 0.90f
                for (i in 0 until segments) {
                    val angleDeg = i * (360f / segments)
                    val sweep = 18f
                    drawArc(
                        color = if (i % 3 == 0) HudAmberGold else HudCyanGlow.copy(alpha = 0.7f),
                        startAngle = angleDeg,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - segRadius, center.y - segRadius),
                        size = Size(segRadius * 2, segRadius * 2),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Geometric outer tick marks
                val tickRadius = maxRadius * 0.98f
                for (j in 0 until 36) {
                    val angleRad = Math.toRadians((j * 10).toDouble())
                    val tickLen = if (j % 3 == 0) 6.dp.toPx() else 3.dp.toPx()
                    val p1 = Offset(
                        center.x + (tickRadius - tickLen) * cos(angleRad).toFloat(),
                        center.y + (tickRadius - tickLen) * sin(angleRad).toFloat()
                    )
                    val p2 = Offset(
                        center.x + tickRadius * cos(angleRad).toFloat(),
                        center.y + tickRadius * sin(angleRad).toFloat()
                    )
                    drawLine(
                        color = HudCyanDim.copy(alpha = 0.6f),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // Middle thin tech line ring
            drawCircle(
                color = HudBorderBright,
                radius = maxRadius * 0.74f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Inner counter-rotating ring with triangular power gates
            rotate(degrees = rotationInner, pivot = center) {
                val innerRadius = maxRadius * 0.62f
                val innerSegments = 8
                for (k in 0 until innerSegments) {
                    val angleDeg = k * (360f / innerSegments)
                    drawArc(
                        color = HudCyanGlow,
                        startAngle = angleDeg,
                        sweepAngle = 26f,
                        useCenter = false,
                        topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                        size = Size(innerRadius * 2, innerRadius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Square)
                    )
                }
            }

            // Core glow circular aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HudCyanGlow.copy(alpha = coreGlowAlpha),
                        HudBlueElectric.copy(alpha = coreGlowAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 0.45f
                ),
                radius = maxRadius * 0.45f,
                center = center
            )
        }

        // Central High-Tech Glowing Mic Button
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isListening) HudAlertRed.copy(alpha = 0.9f)
                            else if (isSpeaking) HudAmberGold.copy(alpha = 0.9f)
                            else HudSurfaceVariant,
                            HudVoidDark
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(HudCyanGlow, HudBlueElectric, HudAmberGold, HudCyanGlow)
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMicClick
                )
                .testTag("reactor_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when {
                        isSpeaking -> Icons.Default.Stop
                        isListening -> Icons.Default.Mic
                        else -> Icons.Default.Mic
                    },
                    contentDescription = "JARVIS ভয়েস বাটন",
                    tint = when {
                        isListening -> Color.White
                        isSpeaking -> HudAmberGold
                        else -> HudCyanGlow
                    },
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        isListening -> "শুনছি..."
                        isSpeaking -> "বলছি..."
                        else -> "JARVIS"
                    },
                    color = when {
                        isListening -> HudAlertRed
                        isSpeaking -> HudAmberGold
                        else -> HudCyanGlow
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
