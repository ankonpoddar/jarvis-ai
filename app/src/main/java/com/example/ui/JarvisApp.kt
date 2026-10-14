package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.AlarmClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiService
import com.example.data.JarvisRepository
import com.example.model.*
import com.example.speech.JarvisSpeechManager
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.HudHeader
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JarvisApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val repository = remember { JarvisRepository(context) }
    val speechManager = remember { JarvisSpeechManager(context) }
    val geminiService = remember { GeminiService() }

    var currentTab by remember { mutableStateOf(JarvisTab.HOME) }

    // State flows from speech manager
    val isSpeaking by speechManager.isSpeaking.collectAsState()
    val isListening by speechManager.isListening.collectAsState()
    val isMuted by speechManager.isMuted.collectAsState()
    val isWakeWordActive by speechManager.isWakeWordListening.collectAsState()

    // App state
    var reminders by remember { mutableStateOf(repository.getReminders()) }
    var events by remember { mutableStateOf(repository.getEvents()) }
    var files by remember { mutableStateOf(repository.getFiles()) }
    var contacts by remember { mutableStateOf(repository.getContacts()) }
    var settings by remember { mutableStateOf(repository.getSettings()) }
    var medicines by remember { mutableStateOf(repository.getBuiltinMedicines()) }

    var lastCommandText by remember { mutableStateOf("সিস্টেম প্রস্তুত। বলুন বা লিখুন...") }
    var jarvisResponseText by remember { mutableStateOf("শুভ দিন স্যার। আমি JARVIS। আপনার সেবায় নিয়োজিত।") }
    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }

    // Audio / Voice recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        speechManager.setListeningState(false)
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                lastCommandText = spokenText
                // Process with Gemini & NLP
                scope.launch {
                    val parsed = geminiService.processUserCommand(spokenText)
                    handleJarvisAction(
                        parsed = parsed,
                        repository = repository,
                        speechManager = speechManager,
                        context = context,
                        onUpdateResponse = { reply, details ->
                            jarvisResponseText = details
                            speechManager.speak(reply)
                        },
                        onSwitchTab = { currentTab = it },
                        onRequireConfirmation = { pendingAction = it },
                        onRefreshData = {
                            reminders = repository.getReminders()
                            events = repository.getEvents()
                        }
                    )
                }
            }
        }
    }

    // Permission launcher for RECORD_AUDIO
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                speechManager.setListeningState(true)
                speechLauncher.launch(speechManager.createSpeechIntent())
            } catch (e: Exception) {
                speechManager.setListeningState(false)
                Toast.makeText(context, "ভয়েস রিকগনিশন শুরু করা যায়নি", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "ভয়েস কমান্ডের জন্য মাইক্রোফোন পারমিশন প্রয়োজন", Toast.LENGTH_LONG).show()
        }
    }

    // Computed Morning Briefing Data
    val briefingData = remember(events, reminders, settings.city) {
        generateBriefingData(events, reminders, settings.city)
    }

    // Check if auto-morning briefing should trigger today on first open
    LaunchedEffect(Unit) {
        if (repository.shouldTriggerMorningBriefingToday()) {
            jarvisResponseText = "শুভ সকাল স্যার! আজকের মর্নিং ব্রিফিং শুরু করছি।"
            speechManager.speak(briefingData.spokenText)
        }
    }

    // Handle BackHandler for non-home tabs
    BackHandler(enabled = currentTab != JarvisTab.HOME) {
        currentTab = JarvisTab.HOME
    }

    Scaffold(
        containerColor = HudVoidDark,
        topBar = {
            HudHeader(
                isWakeWordActive = isWakeWordActive,
                isMuted = isMuted,
                onToggleWakeWord = {
                    val target = !isWakeWordActive
                    val newSettings = settings.copy(isWakeWordEnabled = target)
                    settings = newSettings
                    repository.saveSettings(newSettings)
                    if (target) {
                        speechManager.startContinuousWakeWordListening(
                            onWakeWordTriggered = {
                                jarvisResponseText = "শুনছি স্যার..."
                                speechManager.speak("হ্যাঁ স্যার, বলুন?")
                            },
                            onSpeechCommandResult = { cmd ->
                                lastCommandText = cmd
                                scope.launch {
                                    val parsed = geminiService.processUserCommand(cmd)
                                    handleJarvisAction(
                                        parsed = parsed,
                                        repository = repository,
                                        speechManager = speechManager,
                                        context = context,
                                        onUpdateResponse = { reply, details ->
                                            jarvisResponseText = details
                                            speechManager.speak(reply)
                                        },
                                        onSwitchTab = { currentTab = it },
                                        onRequireConfirmation = { pendingAction = it },
                                        onRefreshData = {
                                            reminders = repository.getReminders()
                                            events = repository.getEvents()
                                        }
                                    )
                                }
                            }
                        )
                    } else {
                        speechManager.stopContinuousWakeWordListening()
                    }
                },
                onToggleMute = {
                    val newMute = !isMuted
                    speechManager.setMuted(newMute)
                    val newSettings = settings.copy(isMuteEnabled = newMute)
                    settings = newSettings
                    repository.saveSettings(newSettings)
                }
            )
        },
        bottomBar = {
            JarvisBottomNavigationBar(
                currentTab = currentTab,
                onTabSelect = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HudVoidDark)
        ) {
            when (currentTab) {
                JarvisTab.HOME -> {
                    HomeScreen(
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        isWakeWordActive = isWakeWordActive,
                        briefingData = briefingData,
                        lastCommandText = lastCommandText,
                        jarvisResponseText = jarvisResponseText,
                        onMicClick = {
                            if (isSpeaking) {
                                speechManager.stopSpeaking()
                            } else {
                                recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onTextSubmit = { text ->
                            lastCommandText = text
                            scope.launch {
                                val parsed = geminiService.processUserCommand(text)
                                handleJarvisAction(
                                    parsed = parsed,
                                    repository = repository,
                                    speechManager = speechManager,
                                    context = context,
                                    onUpdateResponse = { reply, details ->
                                        jarvisResponseText = details
                                        speechManager.speak(reply)
                                    },
                                    onSwitchTab = { currentTab = it },
                                    onRequireConfirmation = { pendingAction = it },
                                    onRefreshData = {
                                        reminders = repository.getReminders()
                                        events = repository.getEvents()
                                    }
                                )
                            }
                        },
                        onPlayBriefingSpeech = {
                            speechManager.speak(briefingData.spokenText)
                        },
                        onStopBriefingSpeech = {
                            speechManager.stopSpeaking()
                        }
                    )
                }

                JarvisTab.REMINDERS -> {
                    RemindersScreen(
                        reminders = reminders,
                        onAddReminder = { title, time, isAlarm ->
                            repository.addReminder(
                                ReminderItem(
                                    title = title,
                                    timeFormatted = time,
                                    isAlarm = isAlarm
                                )
                            )
                            reminders = repository.getReminders()
                            speechManager.speak("$time-এ রিমাইন্ডার সংরক্ষিত হয়েছে।")
                        },
                        onToggleReminder = { id ->
                            repository.toggleReminder(id)
                            reminders = repository.getReminders()
                        },
                        onDeleteReminder = { id ->
                            repository.deleteReminder(id)
                            reminders = repository.getReminders()
                        },
                        onOpenClockApp = {
                            openAndroidClockApp(context)
                        },
                        onTestAlarmAlert = {
                            triggerAlarmSoundAndVibration(context)
                        }
                    )
                }

                JarvisTab.FILES -> {
                    FilesScreen(
                        files = files,
                        onAddFile = { newFile ->
                            repository.addFile(newFile)
                            files = repository.getFiles()
                            speechManager.speak("ফাইল আপলোড হয়েছে এবং প্রস্তাবিত নাম দেয়া হয়েছে।")
                        },
                        onDeleteFile = { id ->
                            repository.deleteFile(id)
                            files = repository.getFiles()
                        },
                        onOpenFile = { fileItem ->
                            openFileIntent(context, fileItem)
                        }
                    )
                }

                JarvisTab.CALENDAR -> {
                    CalendarScreen(
                        events = events,
                        onAddEvent = { title, date, time, notes ->
                            repository.addEvent(
                                CalendarEventItem(
                                    title = title,
                                    dateFormatted = date,
                                    timeFormatted = time,
                                    locationOrNotes = notes
                                )
                            )
                            events = repository.getEvents()
                            speechManager.speak("ইভেন্ট ক্যালেন্ডারে সংরক্ষণ করা হয়েছে।")
                        },
                        onDeleteEvent = { id ->
                            repository.deleteEvent(id)
                            events = repository.getEvents()
                        }
                    )
                }

                JarvisTab.TOOLS -> {
                    ToolsScreen(
                        medicines = medicines,
                        contacts = contacts,
                        settings = settings,
                        onSaveSettings = { updatedSettings ->
                            repository.saveSettings(updatedSettings)
                            settings = updatedSettings
                            Toast.makeText(context, "সেটিংস সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        onAddContact = { contact ->
                            repository.addContact(contact)
                            contacts = repository.getContacts()
                            Toast.makeText(context, "${contact.name} যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteContact = { id ->
                            repository.deleteContact(id)
                            contacts = repository.getContacts()
                        },
                        onTriggerCall = { contact ->
                            pendingAction = PendingAction(
                                type = PendingAction.ActionType.CALL,
                                targetName = "${contact.name} (${contact.phoneNumber})",
                                detail = "সংযোগ স্থাপন করতে 'কল করুন' চাপুন।",
                                extraData = contact.phoneNumber
                            )
                        },
                        onTriggerSms = { contact ->
                            pendingAction = PendingAction(
                                type = PendingAction.ActionType.SMS,
                                targetName = "${contact.name} (${contact.phoneNumber})",
                                detail = "বার্তা: জরুরী যোগাযোগ প্রয়োজন, দয়া করে কলব্যাক করুন।",
                                extraData = contact.phoneNumber
                            )
                        },
                        onSearchMedicineAI = { query ->
                            scope.launch {
                                val reply = "$query ঔষধের ক্লিনিকাল নির্দেশিকা ও প্রাপ্তবয়স্ক ডোজ রেফারেন্স তথ্যাদি স্ক্রিনে দেখানো হলো।"
                                jarvisResponseText = reply
                                speechManager.speak(reply)
                            }
                        },
                        onWebSearchQuery = { query ->
                            scope.launch {
                                val prompt = "গুগল সার্চ ও সাধারণ জ্ঞান: $query এর উত্তর বাংলায় সংক্ষেপে দিন।"
                                val parsed = geminiService.processUserCommand(prompt)
                                jarvisResponseText = parsed.displayDetails
                                speechManager.speak(parsed.spokenReply)
                                currentTab = JarvisTab.HOME
                            }
                        }
                    )
                }
            }

            // Pending Action Confirmation Dialog (Call, SMS, Email)
            ConfirmationDialog(
                action = pendingAction,
                onConfirm = {
                    val act = pendingAction
                    pendingAction = null
                    if (act != null) {
                        when (act.type) {
                            PendingAction.ActionType.CALL -> {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${act.extraData}"))
                                context.startActivity(callIntent)
                            }
                            PendingAction.ActionType.SMS -> {
                                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${act.extraData}")).apply {
                                    putExtra("sms_body", "জরুরী যোগাযোগ প্রয়োজন, দয়া করে কলব্যাক করুন।")
                                }
                                context.startActivity(smsIntent)
                            }
                            PendingAction.ActionType.EMAIL -> {
                                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${act.extraData}")).apply {
                                    putExtra(Intent.EXTRA_SUBJECT, "জরুরী যোগাযোগ - JARVIS")
                                    putExtra(Intent.EXTRA_TEXT, act.detail)
                                }
                                context.startActivity(emailIntent)
                            }
                        }
                    }
                },
                onDismiss = {
                    pendingAction = null
                    speechManager.speak("অনুরোধটি বাতিল করা হয়েছে স্যার।")
                }
            )
        }
    }
}

private fun handleJarvisAction(
    parsed: com.example.ai.JarvisParsedResponse,
    repository: JarvisRepository,
    speechManager: JarvisSpeechManager,
    context: Context,
    onUpdateResponse: (String, String) -> Unit,
    onSwitchTab: (JarvisTab) -> Unit,
    onRequireConfirmation: (PendingAction) -> Unit,
    onRefreshData: () -> Unit
) {
    when (parsed.intent) {
        "ALARM_REMINDER" -> {
            val title = parsed.parameters["title"] ?: "নতুন রিমাইন্ডার"
            val time = parsed.parameters["time"] ?: "07:00 AM"
            repository.addReminder(ReminderItem(title = title, timeFormatted = time, isAlarm = true))
            onRefreshData()
            onUpdateResponse(parsed.spokenReply, "${parsed.displayDetails}\n(রিমাইন্ডার তালিকায় যুক্ত করা হয়েছে)")
            onSwitchTab(JarvisTab.REMINDERS)
        }

        "CALL_CONTACT" -> {
            val name = parsed.parameters["contact_name"] ?: "রহিম"
            val contact = repository.findContactByName(name) ?: repository.getContacts().firstOrNull()
            val phone = contact?.phoneNumber ?: "+8801711000111"
            val displayName = contact?.name ?: name
            onRequireConfirmation(
                PendingAction(
                    type = PendingAction.ActionType.CALL,
                    targetName = "$displayName ($phone)",
                    detail = "কল করার পূর্বে নিশ্চিত করুন।",
                    extraData = phone
                )
            )
            onUpdateResponse(parsed.spokenReply, "$displayName-কে কল করার প্রস্তুতি নেয়া হয়েছে। নিশ্চিত করুন।")
        }

        "SEND_SMS" -> {
            val name = parsed.parameters["contact_name"] ?: "করিম"
            val msg = parsed.parameters["message"] ?: "জরুরী যোগাযোগ প্রয়োজন, দয়া করে কলব্যাক করুন।"
            val contact = repository.findContactByName(name) ?: repository.getContacts().firstOrNull()
            val phone = contact?.phoneNumber ?: "+8801811222333"
            val displayName = contact?.name ?: name
            onRequireConfirmation(
                PendingAction(
                    type = PendingAction.ActionType.SMS,
                    targetName = "$displayName ($phone)",
                    detail = "বার্তা: $msg",
                    extraData = phone
                )
            )
            onUpdateResponse(parsed.spokenReply, "$displayName-কে এসএমএস প্রেরণের ড্রাফট তৈরি করা হয়েছে।")
        }

        "SEND_EMAIL" -> {
            val recipient = parsed.parameters["recipient"] ?: "team@example.com"
            val subject = parsed.parameters["subject"] ?: "জরুরী আপডেট - JARVIS"
            val body = parsed.parameters["body"] ?: "স্যার, নির্দেশ অনুযায়ী ইমেইল ড্রাফট করা হয়েছে।"
            onRequireConfirmation(
                PendingAction(
                    type = PendingAction.ActionType.EMAIL,
                    targetName = recipient,
                    detail = "বিষয়: $subject\nবিবরণ: $body",
                    extraData = recipient
                )
            )
            onUpdateResponse(parsed.spokenReply, "ইমেইল ড্রাফট সম্পন্ন হয়েছে। 'ইমেইল পাঠান' নিশ্চিত করুন।")
        }

        "CALENDAR_EVENT" -> {
            val title = parsed.parameters["title"] ?: "নতুন মিটিং"
            val time = parsed.parameters["time"] ?: "11:00 AM"
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.addEvent(CalendarEventItem(title = title, dateFormatted = todayStr, timeFormatted = time))
            onRefreshData()
            onUpdateResponse(parsed.spokenReply, parsed.displayDetails)
            onSwitchTab(JarvisTab.CALENDAR)
        }

        "MEDICINE_INFO" -> {
            onUpdateResponse(parsed.spokenReply, parsed.displayDetails)
            onSwitchTab(JarvisTab.TOOLS)
        }

        "DAILY_BRIEFING" -> {
            onUpdateResponse(parsed.spokenReply, parsed.displayDetails)
        }

        else -> {
            onUpdateResponse(parsed.spokenReply, parsed.displayDetails)
        }
    }
}

private fun generateBriefingData(
    events: List<CalendarEventItem>,
    reminders: List<ReminderItem>,
    city: String
): MorningBriefingData {
    val dateBn = SimpleDateFormat("d MMMM yyyy", Locale("bn", "BD")).format(Date())
    val dayBn = SimpleDateFormat("EEEE", Locale("bn", "BD")).format(Date())
    val temp = "২৮° সে."
    val summary = "আংশিক মেঘলা ও হালকা রোদ"
    val advice = "বৃষ্টির সম্ভাবনা থাকায় ছাতা সাথে রাখুন"

    val eventsStrList = events.map { "${it.timeFormatted} - ${it.title}" }
    val remindersStrList = reminders.filter { !it.isCompleted }.map { "${it.timeFormatted} - ${it.title}" }

    val spokenBuilder = StringBuilder()
    spokenBuilder.append("শুভ সকাল স্যার। আজ $dayBn, $dateBn। ")
    spokenBuilder.append("$city-র বর্তমান তাপমাত্রা $temp, $summary। $advice। ")
    if (eventsStrList.isNotEmpty()) {
        spokenBuilder.append("আজকে আপনার ${eventsStrList.size}টি নির্ধারিত সূচি রয়েছে। প্রথমটি ${eventsStrList.first()}। ")
    } else {
        spokenBuilder.append("আজকের ক্যালেন্ডারে কোনো জরুরি ইভেন্ট নেই। ")
    }
    if (remindersStrList.isNotEmpty()) {
        spokenBuilder.append("অপেক্ষমান রিমাইন্ডার আছে ${remindersStrList.size}টি। ")
    }
    spokenBuilder.append("JARVIS সিস্টেম আপনার নির্দেশের জন্য অপেক্ষমাণ।")

    return MorningBriefingData(
        dateBangla = dateBn,
        dayBangla = dayBn,
        weatherSummary = summary,
        temperature = temp,
        weatherAdvice = advice,
        eventsList = eventsStrList,
        remindersList = remindersStrList,
        spokenText = spokenBuilder.toString()
    )
}

private fun openAndroidClockApp(context: Context) {
    try {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "ডিভাইস ক্লক অ্যাপ পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
    }
}

private fun triggerAlarmSoundAndVibration(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(800)
        }

        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, notificationUri)
        ringtone?.play()
        Toast.makeText(context, "এলার্ম নোটিফিকেশন অ্যালার্ট পরীক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {}
}

private fun openFileIntent(context: Context, fileItem: JarvisFileItem) {
    try {
        if (fileItem.uriString.isNotBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileItem.uriString)).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "ফাইল প্রিভিউ: ${fileItem.name}", Toast.LENGTH_SHORT).show()
        }
    } catch (_: Exception) {
        Toast.makeText(context, "ফাইলটি খোলার জন্য কোনো উপযুক্ত অ্যাপ নেই", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun JarvisBottomNavigationBar(
    currentTab: JarvisTab,
    onTabSelect: (JarvisTab) -> Unit
) {
    Surface(
        color = HudNavyBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(HudBorderCyan, HudCyanGlow.copy(alpha = 0.5f), HudBorderCyan))
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            JarvisTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                val icon = when (tab) {
                    JarvisTab.HOME -> Icons.Default.Home
                    JarvisTab.REMINDERS -> Icons.Default.Alarm
                    JarvisTab.FILES -> Icons.Default.Folder
                    JarvisTab.CALENDAR -> Icons.Default.CalendarMonth
                    JarvisTab.TOOLS -> Icons.Default.Build
                }

                IconButton(
                    onClick = { onTabSelect(tab) },
                    modifier = Modifier
                        .testTag("nav_tab_${tab.name.lowercase()}")
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.labelBn,
                            tint = if (isSelected) HudCyanGlow else HudTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = tab.labelBn,
                            color = if (isSelected) HudCyanGlow else HudTextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
