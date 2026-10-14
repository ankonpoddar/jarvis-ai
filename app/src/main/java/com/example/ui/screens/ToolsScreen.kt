package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.model.BriefingSettings
import com.example.model.JarvisContact
import com.example.model.MedicineInfo
import com.example.ui.components.HudButton
import com.example.ui.components.HudCard
import com.example.ui.theme.*

enum class ToolsSubTab(val labelBn: String) {
    MEDICINE("ঔষধ তথ্য"),
    CONTACTS("যোগাযোগ (কল/SMS)"),
    WEATHER_WEB("আবহাওয়া ও ওয়েব"),
    SETTINGS("সেটিংস")
}

@Composable
fun ToolsScreen(
    medicines: List<MedicineInfo>,
    contacts: List<JarvisContact>,
    settings: BriefingSettings,
    onSaveSettings: (BriefingSettings) -> Unit,
    onAddContact: (JarvisContact) -> Unit,
    onDeleteContact: (String) -> Unit,
    onTriggerCall: (JarvisContact) -> Unit,
    onTriggerSms: (JarvisContact) -> Unit,
    onSearchMedicineAI: (String) -> Unit,
    onWebSearchQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(ToolsSubTab.MEDICINE) }
    var medicineQuery by remember { mutableStateOf("") }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var webQueryText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Sub-tabs navigation
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(ToolsSubTab.values()) { tab ->
                    val isSelected = selectedSubTab == tab
                    Surface(
                        color = if (isSelected) HudCyanGlow.copy(alpha = 0.25f) else HudSurfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) HudCyanGlow else HudBorderCyan),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.clickable { selectedSubTab = tab }
                    ) {
                        Text(
                            text = tab.labelBn,
                            color = if (isSelected) HudCyanGlow else HudTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        when (selectedSubTab) {
            ToolsSubTab.MEDICINE -> {
                // Medicine Info Helper
                item {
                    HudCard(
                        title = "ঔষধ তথ্য সহায়ক // MEDICINE HELPER",
                        icon = Icons.Default.MedicalServices
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "যে কোনো ঔষধের নাম লিখে অনুসন্ধান করুন বা ভয়েস কমান্ড দিন:",
                                color = HudTextSecondary,
                                fontSize = 12.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = medicineQuery,
                                    onValueChange = { medicineQuery = it },
                                    placeholder = { Text("যেমন: Napa, Seclo, Fexo...", color = HudTextMuted) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("medicine_search_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HudCyanGlow,
                                        unfocusedBorderColor = HudBorderCyan,
                                        focusedTextColor = HudTextPrimary,
                                        unfocusedTextColor = HudTextPrimary
                                    )
                                )
                                Button(
                                    onClick = {
                                        if (medicineQuery.isNotBlank()) {
                                            onSearchMedicineAI(medicineQuery)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HudCyanGlow,
                                        contentColor = HudVoidDark
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(50.dp)
                                ) {
                                    Text("খুঁজুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Disclaimer Banner
                item {
                    Surface(
                        color = HudAmberGold.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, HudAmberGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = HudAmberGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "রেফারেন্স তথ্য: এই তথ্য স্বাস্থ্যসেবা পেশাদারদের রেফারেন্সের জন্য। সেবনের আগে অফিশিয়াল রেফারেন্স ও চিকিৎসকের পরামর্শ নিন।",
                                color = HudAmberGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Medicines List
                val displayedMeds = if (medicineQuery.isBlank()) medicines else {
                    medicines.filter {
                        it.tradeName.contains(medicineQuery, ignoreCase = true) ||
                        it.genericName.contains(medicineQuery, ignoreCase = true)
                    }
                }

                items(displayedMeds) { med ->
                    MedicineDetailCard(med)
                }
            }

            ToolsSubTab.CONTACTS -> {
                // Contacts Section for Call & SMS
                item {
                    HudCard(
                        title = "সংরক্ষিত যোগাযোগ // CONTACTS",
                        icon = Icons.Default.Contacts,
                        cornerAction = {
                            Button(
                                onClick = { showAddContactDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HudCyanGlow,
                                    contentColor = HudVoidDark
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    ) {
                        Text(
                            text = "ভয়েস কমান্ড যেমন: \"রহিমকে কল দাও\" বা \"করিমকে এসএমএস পাঠাও\" এখানে কাজ করে। প্রেরণের আগে নিশ্চিতকরণ চাওয়া হবে।",
                            color = HudTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                items(contacts, key = { it.id }) { contact ->
                    ContactCard(
                        contact = contact,
                        onCall = { onTriggerCall(contact) },
                        onSms = { onTriggerSms(contact) },
                        onDelete = { onDeleteContact(contact.id) }
                    )
                }
            }

            ToolsSubTab.WEATHER_WEB -> {
                // Web Search & Weather Grounding
                item {
                    HudCard(
                        title = "আবহাওয়া ও ওয়েব অনুসন্ধান",
                        icon = Icons.Default.CloudQueue
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "গুগল সার্চ ও জেমিনাই এর মাধ্যমে রিয়েল-টাইম তথ্য অনুসন্ধান:",
                                color = HudTextSecondary,
                                fontSize = 12.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = webQueryText,
                                    onValueChange = { webQueryText = it },
                                    placeholder = { Text("যেমন: আজকের ঢাকা আবহাওয়া, আজকের খবর...", color = HudTextMuted) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HudCyanGlow,
                                        unfocusedBorderColor = HudBorderCyan,
                                        focusedTextColor = HudTextPrimary,
                                        unfocusedTextColor = HudTextPrimary
                                    )
                                )
                                Button(
                                    onClick = {
                                        if (webQueryText.isNotBlank()) {
                                            onWebSearchQuery(webQueryText)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HudCyanGlow,
                                        contentColor = HudVoidDark
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("সার্চ", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            ToolsSubTab.SETTINGS -> {
                // Morning Briefing & System Settings
                item {
                    HudCard(
                        title = "JARVIS সেটিংস ও মর্নিং ব্রিফিং",
                        icon = Icons.Default.Settings
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Auto Briefing Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "দৈনিক সকালের ব্রিফিং সক্রিয়",
                                        color = HudTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "সকালে প্রথমবার অ্যাপ খুললে অটোমেটিক ব্রিফিং দেওয়া হবে",
                                        color = HudTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = settings.isAutoBriefingEnabled,
                                    onCheckedChange = {
                                        onSaveSettings(settings.copy(isAutoBriefingEnabled = it))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HudCyanGlow,
                                        checkedTrackColor = HudCyanDim.copy(alpha = 0.4f)
                                    )
                                )
                            }

                            HorizontalDivider(color = HudBorderCyan)

                            // Wake word toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "কন্টিনিউয়াস ওয়েক ওয়ার্ড (\"Hey Jarvis\")",
                                        color = HudTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "অ্যাপ খোলা থাকা অবস্থায় সার্বক্ষণিক নির্দেশ শুনতে থাকবে",
                                        color = HudTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = settings.isWakeWordEnabled,
                                    onCheckedChange = {
                                        onSaveSettings(settings.copy(isWakeWordEnabled = it))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HudAmberGold,
                                        checkedTrackColor = HudAmberGold.copy(alpha = 0.4f)
                                    )
                                )
                            }

                            HorizontalDivider(color = HudBorderCyan)

                            // City Input
                            var cityText by remember { mutableStateOf(settings.city) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = cityText,
                                    onValueChange = { cityText = it },
                                    label = { Text("আবহাওয়ার শহর", color = HudTextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HudCyanGlow,
                                        unfocusedBorderColor = HudBorderCyan,
                                        focusedTextColor = HudTextPrimary,
                                        unfocusedTextColor = HudTextPrimary
                                    )
                                )
                                Button(
                                    onClick = { onSaveSettings(settings.copy(city = cityText)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HudCyanGlow,
                                        contentColor = HudVoidDark
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("সেভ", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onConfirm = { contact ->
                onAddContact(contact)
                showAddContactDialog = false
            }
        )
    }
}

@Composable
fun MedicineDetailCard(med: MedicineInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, HudBorderCyan), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HudSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = med.tradeName,
                    color = HudCyanGlow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = med.genericName,
                    color = HudAmberGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(color = HudBorderCyan, modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "ব্যবহার / নির্দেশনা: ${med.indications}",
                color = HudTextPrimary,
                fontSize = 12.sp
            )

            Text(
                text = "প্রাপ্তবয়স্ক ডোজ: ${med.dosageAdult}",
                color = HudCyanDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "পার্শ্বপ্রতিক্রিয়া: ${med.sideEffects}",
                color = HudTextSecondary,
                fontSize = 11.sp
            )

            Text(
                text = "সতর্কতা ও মিথস্ক্রিয়া: ${med.warnings}",
                color = HudAlertRed.copy(alpha = 0.9f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ContactCard(
    contact: JarvisContact,
    onCall: () -> Unit,
    onSms: () -> Unit,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = HudCyanGlow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            color = HudTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (contact.relation.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${contact.relation})",
                                color = HudAmberGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = contact.phoneNumber,
                        color = HudTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onCall) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "কল", tint = HudSuccessGreen)
                }
                IconButton(onClick = onSms) {
                    Icon(imageVector = Icons.Default.Message, contentDescription = "এসএমএস", tint = HudCyanGlow)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "মুছুন", tint = HudAlertRed)
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (JarvisContact) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }

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
                    text = "নতুন পরিচিতি যুক্ত করুন",
                    color = HudCyanGlow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("নাম (যেমন: রহিম)", color = HudTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HudCyanGlow,
                        unfocusedBorderColor = HudBorderCyan,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ফোন নম্বর (যেমন: 01711...)", color = HudTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HudCyanGlow,
                        unfocusedBorderColor = HudBorderCyan,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("ইমেইল (ঐচ্ছিক)", color = HudTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HudCyanGlow,
                        unfocusedBorderColor = HudBorderCyan,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    )
                )

                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("সম্পর্ক (যেমন: বন্ধু, পরিবার)", color = HudTextSecondary) },
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
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onConfirm(
                                    JarvisContact(
                                        id = System.currentTimeMillis().toString(),
                                        name = name,
                                        phoneNumber = phone,
                                        email = email,
                                        relation = relation
                                    )
                                )
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
