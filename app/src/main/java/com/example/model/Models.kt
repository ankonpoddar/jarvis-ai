package com.example.model

enum class JarvisTab(val labelBn: String) {
    HOME("হোম"),
    REMINDERS("রিমাইন্ডার"),
    FILES("ফাইলস"),
    CALENDAR("ক্যালেন্ডার"),
    TOOLS("টুলস")
}

data class ReminderItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val timeFormatted: String,
    val isAlarm: Boolean = true,
    val isCompleted: Boolean = false,
    val dateEpochMillis: Long = System.currentTimeMillis()
)

data class CalendarEventItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val dateFormatted: String,
    val timeFormatted: String,
    val locationOrNotes: String = ""
)

enum class FileCategory(val labelBn: String) {
    DOCUMENTS("ডকুমেন্টস"),
    IMAGES("ছবি"),
    PRESCRIPTIONS("প্রেসক্রিপশন"),
    RECEIPTS("রশিদ"),
    OTHER("অন্যান্য")
}

data class JarvisFileItem(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val suggestedName: String,
    val category: FileCategory,
    val uriString: String,
    val sizeFormatted: String,
    val dateAdded: String
)

data class JarvisContact(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val relation: String = ""
)

data class MedicineInfo(
    val tradeName: String,
    val genericName: String,
    val indications: String,
    val dosageAdult: String,
    val sideEffects: String,
    val interactions: String,
    val warnings: String,
    val verificationNotice: String = "⚠️ এটি শুধুমাত্র রেফারেন্স তথ্যের জন্য। ব্যবহারের আগে রেজিস্টার্ড চিকিৎসকের পরামর্শ নিন।"
)

data class PendingAction(
    val type: ActionType,
    val targetName: String,
    val detail: String,
    val extraData: String = ""
) {
    enum class ActionType {
        CALL,
        SMS,
        EMAIL
    }
}

data class MorningBriefingData(
    val dateBangla: String,
    val dayBangla: String,
    val weatherSummary: String,
    val temperature: String,
    val weatherAdvice: String,
    val eventsList: List<String>,
    val remindersList: List<String>,
    val spokenText: String
)

data class BriefingSettings(
    val briefingTime: String = "07:30 AM",
    val isAutoBriefingEnabled: Boolean = true,
    val city: String = "ঢাকা",
    val isWakeWordEnabled: Boolean = false,
    val isMuteEnabled: Boolean = false
)
