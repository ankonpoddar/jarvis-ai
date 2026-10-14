package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PendingAction
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HudHeader(
    isWakeWordActive: Boolean,
    isMuted: Boolean,
    onToggleWakeWord: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTime = remember {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date())
    }
    val currentDateBn = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM", Locale("bn", "BD"))
        try { sdf.format(Date()) } catch (_: Exception) { "১২ অক্টোবর ২০২৬" }
    }

    Surface(
        color = HudNavyBg.copy(alpha = 0.85f),
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(HudBorderCyan, HudCyanGlow.copy(alpha = 0.5f), HudBorderCyan)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: HUD Title & System Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(HudCyanGlow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JARVIS MK-85",
                                color = HudCyanGlow,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ONLINE",
                                color = HudSuccessGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .border(0.5.dp, HudSuccessGreen, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "CORE: ARC-100% // BN-BD ACTIVE",
                            color = HudTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Right: Controls (Wake word indicator & Mute button)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Wake Word Button / Indicator
                    IconButton(
                        onClick = onToggleWakeWord,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("wake_word_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isWakeWordActive) Icons.Default.Hearing else Icons.Default.HearingDisabled,
                            contentDescription = "ওয়েক ওয়ার্ড টগল",
                            tint = if (isWakeWordActive) HudAmberGold else HudTextMuted
                        )
                    }

                    // Mute Button
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "শব্দ মিউট টগল",
                            tint = if (isMuted) HudAlertRed else HudCyanGlow
                        )
                    }
                }
            }

            // Bottom Sub-line with Bangla Date & Clock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentDateBn,
                    color = HudTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = currentTime,
                    color = HudCyanGlow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HudCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    cornerAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(HudCyanGlow.copy(alpha = 0.5f), HudBorderCyan)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HudCardBg.copy(alpha = 0.90f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = HudCyanGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = title,
                        color = HudCyanGlow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                cornerAction?.invoke()
            }

            // Tech Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(HudCyanGlow.copy(alpha = 0.6f), HudBorderCyan, Color.Transparent)
                        )
                    )
            )

            content()
        }
    }
}

@Composable
fun HudButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isDestructive: Boolean = false,
    isPrimary: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .testTag("hud_button_${text.replace(" ", "_")}"),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (isDestructive) HudAlertRed else if (isPrimary) HudCyanGlow else HudCyanDim
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) HudAlertRed.copy(alpha = 0.2f)
            else if (isPrimary) HudCyanGlow.copy(alpha = 0.18f)
            else HudSurfaceVariant,
            contentColor = if (isDestructive) HudAlertRed else HudCyanGlow
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConfirmationDialog(
    action: PendingAction?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (action == null) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = HudVoidDark,
            border = BorderStroke(1.5.dp, HudAmberGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when (action.type) {
                        PendingAction.ActionType.CALL -> Icons.Default.Phone
                        PendingAction.ActionType.SMS -> Icons.Default.Message
                        PendingAction.ActionType.EMAIL -> Icons.Default.Email
                    },
                    contentDescription = null,
                    tint = HudAmberGold,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when (action.type) {
                        PendingAction.ActionType.CALL -> "কল নিশ্চিতকরণ"
                        PendingAction.ActionType.SMS -> "এসএমএস নিশ্চিতকরণ"
                        PendingAction.ActionType.EMAIL -> "ইমেইল প্রেরণ নিশ্চিতকরণ"
                    },
                    color = HudAmberGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "প্রাপক / লক্ষ্য: ${action.targetName}",
                    color = HudTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = action.detail,
                    color = HudTextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, HudAlertRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HudAlertRed)
                    ) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HudCyanGlow,
                            contentColor = HudVoidDark
                        )
                    ) {
                        Text(
                            text = when (action.type) {
                                PendingAction.ActionType.CALL -> "কল করুন"
                                PendingAction.ActionType.SMS -> "এসএমএস পাঠান"
                                PendingAction.ActionType.EMAIL -> "ইমেইল পাঠান"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
