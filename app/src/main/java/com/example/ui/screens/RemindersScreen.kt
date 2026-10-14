package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.model.ReminderItem
import com.example.ui.components.HudButton
import com.example.ui.components.HudCard
import com.example.ui.theme.*

@Composable
fun RemindersScreen(
    reminders: List<ReminderItem>,
    onAddReminder: (String, String, Boolean) -> Unit,
    onToggleReminder: (String) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onOpenClockApp: () -> Unit,
    onTestAlarmAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Quick Action Bar
        item {
            HudCard(
                title = "এলার্ম ও ঘড়ি নিয়ন্ত্রণ",
                icon = Icons.Default.AccessTime
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HudButton(
                        text = "ক্লক অ্যাপ খুলুন",
                        icon = Icons.Default.Schedule,
                        onClick = onOpenClockApp,
                        modifier = Modifier.weight(1f)
                    )
                    HudButton(
                        text = "সাউন্ড টেস্ট",
                        icon = Icons.Default.NotificationsActive,
                        onClick = onTestAlarmAlert,
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section Title & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সংরক্ষিত রিমাইন্ডার ও এলার্ম (${reminders.size})",
                    color = HudCyanGlow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HudCyanGlow,
                        contentColor = HudVoidDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_reminder_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (reminders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "কোনো রিমাইন্ডার বা এলার্ম সেট করা নেই।\nভয়েস কমান্ড দিয়ে বলুন: \"কাল সকাল ৭টায় এলার্ম দাও\"",
                        color = HudTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(reminders, key = { it.id }) { item ->
                ReminderCard(
                    item = item,
                    onToggle = { onToggleReminder(item.id) },
                    onDelete = { onDeleteReminder(item.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, time, isAlarm ->
                onAddReminder(title, time, isAlarm)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderCard(
    item: ReminderItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    if (item.isCompleted) HudTextMuted else HudCyanGlow.copy(alpha = 0.4f)
                ),
                RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HudSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = HudCyanGlow,
                        uncheckedColor = HudTextSecondary,
                        checkmarkColor = HudVoidDark
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = item.title,
                        color = if (item.isCompleted) HudTextMuted else HudTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (item.isAlarm) Icons.Default.Alarm else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (item.isAlarm) HudAmberGold else HudCyanDim,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.timeFormatted,
                            color = HudCyanGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
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
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("07:00 AM") }
    var isAlarm by remember { mutableStateOf(true) }

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
                    text = "নতুন রিমাইন্ডার বা এলার্ম",
                    color = HudCyanGlow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("শিরোনাম (যেমন: সকালের ঔষধ)", color = HudTextSecondary) },
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
                    label = { Text("সময় (যেমন: 07:00 AM)", color = HudTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HudCyanGlow,
                        unfocusedBorderColor = HudBorderCyan,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isAlarm,
                        onCheckedChange = { isAlarm = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HudCyanGlow,
                            checkedTrackColor = HudCyanDim.copy(alpha = 0.4f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAlarm) "এলার্ম মোড (অডিও ও ভাইব্রেশন)" else "সাধারণ নোটিফিকেশন",
                        color = HudTextPrimary,
                        fontSize = 13.sp
                    )
                }

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
                                onConfirm(title, time, isAlarm)
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
