package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MorningBriefingData
import com.example.ui.components.ArcReactorView
import com.example.ui.components.HudButton
import com.example.ui.components.HudCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    isListening: Boolean,
    isSpeaking: Boolean,
    isWakeWordActive: Boolean,
    briefingData: MorningBriefingData,
    lastCommandText: String,
    jarvisResponseText: String,
    onMicClick: () -> Unit,
    onTextSubmit: (String) -> Unit,
    onPlayBriefingSpeech: () -> Unit,
    onStopBriefingSpeech: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var isBriefingExpanded by remember { mutableStateOf(true) }

    val quickCommands = listOf(
        "আজকের ব্রিফিং দাও",
        "কাল সকাল ৭টায় এলার্ম দাও",
        "রহিমকে কল দাও",
        "করিমকে এসএমএস পাঠাও",
        "নাপা ঔষধের তথ্য দাও",
        "আজকের আবহাওয়া কেমন?"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // 1. Wake word active indicator
        if (isWakeWordActive) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HudAmberGold.copy(alpha = 0.15f))
                        .border(1.dp, HudAmberGold, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(HudAmberGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ওয়েক ওয়ার্ড মোড সক্রিয়: \"Hey Jarvis\" অথবা \"হেই জার্ভিস\" বলুন",
                        color = HudAmberGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Iron Man Arc Reactor with Centered Mic
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                ArcReactorView(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isWakeWordActive = isWakeWordActive,
                    onMicClick = onMicClick
                )
            }
        }

        // 3. Quick Action Chips
        item {
            Column {
                Text(
                    text = "দ্রুত কমান্ড সমূহ:",
                    color = HudTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickCommands) { cmd ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(HudSurfaceVariant)
                                .border(1.dp, HudBorderCyan, RoundedCornerShape(16.dp))
                                .clickable { onTextSubmit(cmd) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cmd,
                                color = HudTextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Daily Morning Briefing HUD Card
        item {
            HudCard(
                title = "দৈনিক মর্নিং ব্রিফিং // MORNING HUD",
                icon = Icons.Default.WbSunny,
                cornerAction = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (isSpeaking) onStopBriefingSpeech() else onPlayBriefingSpeech()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.StopCircle else Icons.Default.VolumeUp,
                                contentDescription = if (isSpeaking) "থামান" else "ব্রিফিং শুনুন",
                                tint = if (isSpeaking) HudAlertRed else HudCyanGlow
                            )
                        }
                        IconButton(
                            onClick = { isBriefingExpanded = !isBriefingExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isBriefingExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "টগল ব্রিফিং",
                                tint = HudTextSecondary
                            )
                        }
                    }
                }
            ) {
                AnimatedVisibility(visible = isBriefingExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Date & Weather line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${briefingData.dayBangla}, ${briefingData.dateBangla}",
                                    color = HudAmberGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "আবহাওয়া: ${briefingData.weatherSummary} (${briefingData.temperature})",
                                    color = HudTextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                            // Weather advice badge
                            Surface(
                                color = HudBlueElectric.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, HudBlueElectric),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = briefingData.weatherAdvice,
                                    color = HudCyanGlow,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Agenda & Reminders
                        if (briefingData.eventsList.isNotEmpty()) {
                            Text(
                                text = "আজকের শিডিউল:",
                                color = HudTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            briefingData.eventsList.forEach { evt ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = HudCyanGlow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = evt,
                                        color = HudTextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (briefingData.remindersList.isNotEmpty()) {
                            Text(
                                text = "আসন্ন রিমাইন্ডার ও এলার্ম:",
                                color = HudTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            briefingData.remindersList.forEach { rem ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = HudAmberGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rem,
                                        color = HudTextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Voice Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HudButton(
                                text = if (isSpeaking) "থামান (Skip)" else "ব্রিফিং শুনুন",
                                onClick = {
                                    if (isSpeaking) onStopBriefingSpeech() else onPlayBriefingSpeech()
                                },
                                icon = if (isSpeaking) Icons.Default.Stop else Icons.Default.Campaign,
                                isDestructive = isSpeaking,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 5. JARVIS Live Console Log
        item {
            HudCard(
                title = "JARVIS কনসোল // RESPONSE LOG",
                icon = Icons.Default.Terminal
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (lastCommandText.isNotBlank()) {
                        Row {
                            Text(
                                text = "YOU: ",
                                color = HudAmberGold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = lastCommandText,
                                color = HudTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row {
                        Text(
                            text = "JARVIS: ",
                            color = HudCyanGlow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = jarvisResponseText,
                            color = HudCyanGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 6. Text Command Input Box
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HudSurface)
                    .border(1.dp, HudBorderCyan, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "টেক্সট লিখে নির্দেশ দিন...",
                            color = HudTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("text_command_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary,
                        cursorColor = HudCyanGlow
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            val msg = textInput
                            textInput = ""
                            onTextSubmit(msg)
                        }
                    },
                    modifier = Modifier.testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "পাঠান",
                        tint = if (textInput.isNotBlank()) HudCyanGlow else HudTextMuted
                    )
                }
            }
        }
    }
}
