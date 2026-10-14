package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CalendarEventItem
import com.example.ui.components.HudButton
import com.example.ui.components.HudCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    events: List<CalendarEventItem>,
    onAddEvent: (String, String, String, String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Daily Agenda Header Card
        item {
            HudCard(
                title = "দৈনিক সময়সূচী ও এজেন্ডা // AGENDA",
                icon = Icons.Default.CalendarToday
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "আজকের নির্ধারিত ইভেন্ট: ${events.size} টি",
                            color = HudCyanGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "স্মার্ট অটো-সিডিউল সক্রিয় রয়েছে",
                            color = HudTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HudCyanGlow,
                            contentColor = HudVoidDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_event_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন ইভেন্ট", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Events List
        item {
            Text(
                text = "আসন্ন সূচি ও মিটিং সমূহ:",
                color = HudAmberGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোনো ইভেন্ট যুক্ত করা নেই।\nভয়েস কমান্ড দিয়ে বলুন: \"কাল সকাল ১০টায় মিটিং শিডিউল করো\"",
                        color = HudTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(events, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    onDelete = { onDeleteEvent(event.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            todayDefault = todayStr,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, date, time, notes ->
                onAddEvent(title, date, time, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EventCard(
    event: CalendarEventItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, HudBorderCyan), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HudSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Time Badge
                Surface(
                    color = HudCyanGlow.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, HudCyanGlow),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = event.timeFormatted,
                            color = HudCyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = event.dateFormatted,
                            color = HudTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }

                Column {
                    Text(
                        text = event.title,
                        color = HudTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (event.locationOrNotes.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = HudAmberGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.locationOrNotes,
                                color = HudTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "মুছুন",
                    tint = HudAlertRed
                )
            }
        }
    }
}

@Composable
fun AddEventDialog(
    todayDefault: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayDefault) }
    var time by remember { mutableStateOf("10:00 AM") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = HudVoidDark,
            border = BorderStroke(1.dp, HudCyanGlow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "নতুন ক্যালেন্ডার ইভেন্ট",
                    color = HudCyanGlow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("ইভেন্টের নাম / বিষয়", color = HudTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HudCyanGlow,
                        unfocusedBorderColor = HudBorderCyan,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("তারিখ (YYYY-MM-DD)", color = HudTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HudCyanGlow,
                        unfocusedBorderColor = HudBorderCyan,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("সময় (যেমন: 10:00 AM)", color = HudTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HudCyanGlow,
                        unfocusedBorderColor = HudBorderCyan,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("স্থান বা অতিরিক্ত নোট", color = HudTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HudCyanGlow,
                        unfocusedBorderColor = HudBorderCyan,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = HudAlertRed)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, date, time, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HudCyanGlow,
                            contentColor = HudVoidDark
                        )
                    ) {
                        Text("সংরক্ষণ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
