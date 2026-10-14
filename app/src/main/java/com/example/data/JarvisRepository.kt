package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class JarvisRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REMINDERS = "key_reminders"
        private const val KEY_EVENTS = "key_events"
        private const val KEY_FILES = "key_files"
        private const val KEY_CONTACTS = "key_contacts"
        private const val KEY_SETTINGS_TIME = "key_settings_time"
        private const val KEY_SETTINGS_AUTO = "key_settings_auto"
        private const val KEY_SETTINGS_CITY = "key_settings_city"
        private const val KEY_SETTINGS_WAKE = "key_settings_wake"
        private const val KEY_SETTINGS_MUTE = "key_settings_mute"
        private const val KEY_LAST_BRIEFING_DATE = "key_last_briefing_date"
    }

    init {
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        if (!prefs.contains(KEY_CONTACTS)) {
            val defaultContacts = listOf(
                JarvisContact(id = "1", name = "রহিম", phoneNumber = "+8801711000111", email = "rahim@example.com", relation = "বন্ধু"),
                JarvisContact(id = "2", name = "করিম", phoneNumber = "+8801811222333", email = "karim@example.com", relation = "সহকর্মী"),
                JarvisContact(id = "3", name = "বাবা", phoneNumber = "+8801911444555", email = "father@example.com", relation = "পরিবার"),
                JarvisContact(id = "4", name = "মা", phoneNumber = "+8801611666777", email = "mother@example.com", relation = "পরিবার"),
                JarvisContact(id = "5", name = "ডাক্তার", phoneNumber = "+8801511888999", email = "doctor@example.com", relation = "চিকিৎসক")
            )
            saveContacts(defaultContacts)
        }

        if (!prefs.contains(KEY_REMINDERS)) {
            val defaultReminders = listOf(
                ReminderItem(id = "1", title = "সকাল ৮টায় নাপা ঔষধ খাওয়ার রিমাইন্ডার", timeFormatted = "08:00 AM", isAlarm = true, isCompleted = false),
                ReminderItem(id = "2", title = "টিম মিটিং পর্যালোচনা", timeFormatted = "02:30 PM", isAlarm = false, isCompleted = false)
            )
            saveReminders(defaultReminders)
        }

        if (!prefs.contains(KEY_EVENTS)) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val defaultEvents = listOf(
                CalendarEventItem(id = "1", title = "সকালের প্রজেক্ট আলোচনা", dateFormatted = todayStr, timeFormatted = "10:30 AM", locationOrNotes = "অনলাইন কনফারেন্স"),
                CalendarEventItem(id = "2", title = "ডাঃ রহমানের সাথে ফলোআপ", dateFormatted = todayStr, timeFormatted = "06:00 PM", locationOrNotes = "স্কয়ার হাসপাতাল")
            )
            saveEvents(defaultEvents)
        }

        if (!prefs.contains(KEY_FILES)) {
            val defaultFiles = listOf(
                JarvisFileItem(
                    id = "1",
                    name = "prescription_cardiology_2026.pdf",
                    suggestedName = "Prescription_Dr_Rahman_Cardiac_Oct2026.pdf",
                    category = FileCategory.PRESCRIPTIONS,
                    uriString = "",
                    sizeFormatted = "1.2 MB",
                    dateAdded = "১২ অক্টোবর ২০২৬"
                ),
                JarvisFileItem(
                    id = "2",
                    name = "national_id_copy.jpg",
                    suggestedName = "Doc_National_ID_Card.jpg",
                    category = FileCategory.DOCUMENTS,
                    uriString = "",
                    sizeFormatted = "850 KB",
                    dateAdded = "১০ অক্টোবর ২০২৬"
                )
            )
            saveFiles(defaultFiles)
        }
    }

    // Reminders
    fun getReminders(): List<ReminderItem> {
        val json = prefs.getString(KEY_REMINDERS, "[]") ?: "[]"
        val list = mutableListOf<ReminderItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ReminderItem(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        title = obj.optString("title", ""),
                        timeFormatted = obj.optString("timeFormatted", ""),
                        isAlarm = obj.optBoolean("isAlarm", true),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        dateEpochMillis = obj.optLong("dateEpochMillis", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveReminders(items: List<ReminderItem>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("timeFormatted", item.timeFormatted)
                put("isAlarm", item.isAlarm)
                put("isCompleted", item.isCompleted)
                put("dateEpochMillis", item.dateEpochMillis)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_REMINDERS, arr.toString()).apply()
    }

    fun addReminder(item: ReminderItem) {
        val list = getReminders().toMutableList()
        list.add(0, item)
        saveReminders(list)
    }

    fun toggleReminder(id: String) {
        val list = getReminders().map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        saveReminders(list)
    }

    fun deleteReminder(id: String) {
        val list = getReminders().filterNot { it.id == id }
        saveReminders(list)
    }

    // Events
    fun getEvents(): List<CalendarEventItem> {
        val json = prefs.getString(KEY_EVENTS, "[]") ?: "[]"
        val list = mutableListOf<CalendarEventItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CalendarEventItem(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        title = obj.optString("title", ""),
                        dateFormatted = obj.optString("dateFormatted", ""),
                        timeFormatted = obj.optString("timeFormatted", ""),
                        locationOrNotes = obj.optString("locationOrNotes", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveEvents(items: List<CalendarEventItem>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("dateFormatted", item.dateFormatted)
                put("timeFormatted", item.timeFormatted)
                put("locationOrNotes", item.locationOrNotes)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_EVENTS, arr.toString()).apply()
    }

    fun addEvent(item: CalendarEventItem) {
        val list = getEvents().toMutableList()
        list.add(0, item)
        saveEvents(list)
    }

    fun deleteEvent(id: String) {
        val list = getEvents().filterNot { it.id == id }
        saveEvents(list)
    }

    // Contacts
    fun getContacts(): List<JarvisContact> {
        val json = prefs.getString(KEY_CONTACTS, "[]") ?: "[]"
        val list = mutableListOf<JarvisContact>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    JarvisContact(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", ""),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        email = obj.optString("email", ""),
                        relation = obj.optString("relation", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveContacts(items: List<JarvisContact>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("phoneNumber", item.phoneNumber)
                put("email", item.email)
                put("relation", item.relation)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS, arr.toString()).apply()
    }

    fun addContact(contact: JarvisContact) {
        val list = getContacts().toMutableList()
        list.add(0, contact)
        saveContacts(list)
    }

    fun deleteContact(id: String) {
        val list = getContacts().filterNot { it.id == id }
        saveContacts(list)
    }

    fun findContactByName(query: String): JarvisContact? {
        val cleaned = query.trim().lowercase(Locale.ROOT)
        return getContacts().firstOrNull {
            it.name.lowercase(Locale.ROOT).contains(cleaned) ||
            cleaned.contains(it.name.lowercase(Locale.ROOT))
        }
    }

    // Files
    fun getFiles(): List<JarvisFileItem> {
        val json = prefs.getString(KEY_FILES, "[]") ?: "[]"
        val list = mutableListOf<JarvisFileItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    JarvisFileItem(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", ""),
                        suggestedName = obj.optString("suggestedName", ""),
                        category = FileCategory.valueOf(obj.optString("category", FileCategory.OTHER.name)),
                        uriString = obj.optString("uriString", ""),
                        sizeFormatted = obj.optString("sizeFormatted", ""),
                        dateAdded = obj.optString("dateAdded", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveFiles(items: List<JarvisFileItem>) {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("suggestedName", item.suggestedName)
                put("category", item.category.name)
                put("uriString", item.uriString)
                put("sizeFormatted", item.sizeFormatted)
                put("dateAdded", item.dateAdded)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_FILES, arr.toString()).apply()
    }

    fun addFile(file: JarvisFileItem) {
        val list = getFiles().toMutableList()
        list.add(0, file)
        saveFiles(list)
    }

    fun deleteFile(id: String) {
        val list = getFiles().filterNot { it.id == id }
        saveFiles(list)
    }

    // Settings
    fun getSettings(): BriefingSettings {
        return BriefingSettings(
            briefingTime = prefs.getString(KEY_SETTINGS_TIME, "07:30 AM") ?: "07:30 AM",
            isAutoBriefingEnabled = prefs.getBoolean(KEY_SETTINGS_AUTO, true),
            city = prefs.getString(KEY_SETTINGS_CITY, "ঢাকা") ?: "ঢাকা",
            isWakeWordEnabled = prefs.getBoolean(KEY_SETTINGS_WAKE, false),
            isMuteEnabled = prefs.getBoolean(KEY_SETTINGS_MUTE, false)
        )
    }

    fun saveSettings(settings: BriefingSettings) {
        prefs.edit()
            .putString(KEY_SETTINGS_TIME, settings.briefingTime)
            .putBoolean(KEY_SETTINGS_AUTO, settings.isAutoBriefingEnabled)
            .putString(KEY_SETTINGS_CITY, settings.city)
            .putBoolean(KEY_SETTINGS_WAKE, settings.isWakeWordEnabled)
            .putBoolean(KEY_SETTINGS_MUTE, settings.isMuteEnabled)
            .apply()
    }

    fun shouldTriggerMorningBriefingToday(): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString(KEY_LAST_BRIEFING_DATE, "")
        val settings = getSettings()
        if (settings.isAutoBriefingEnabled && lastDate != todayStr) {
            prefs.edit().putString(KEY_LAST_BRIEFING_DATE, todayStr).apply()
            return true
        }
        return false
    }

    // Medicine Knowledge
    fun getBuiltinMedicines(): List<MedicineInfo> {
        return listOf(
            MedicineInfo(
                tradeName = "Napa (নাপা)",
                genericName = "Paracetamol (প্যারাসিটামল) ৫০০ মি.গ্রা.",
                indications = "জ্বর, মাথা ব্যথা, শরীর ব্যথা, সাধারণ ঠাণ্ডা ও মৃদু থেকে মাঝারি ব্যথা উপশম।",
                dosageAdult = "১-২টি ট্যাবলেট প্রতি ৪-৬ ঘণ্টা অন্তর (দৈনিক সর্বোচ্চ ৪ গ্রাম বা ৮টি ট্যাবলেট)।",
                sideEffects = "বিরল ক্ষেত্রে ত্বকে ফুসকুড়ি, দীর্ঘমেয়াদী অতিরিক্ত মাত্রায় হেপাটোটক্সিসিটি (লিভারের ক্ষতি)।",
                interactions = "অ্যালকোহল ও ওয়ারফারিনের সাথে সতর্ক ব্যবহার প্রয়োজন।",
                warnings = "লিভার বা কিডনি রোগে আক্রান্ত রোগীদের ক্ষেত্রে ডোজ সমন্বয় করতে হবে।"
            ),
            MedicineInfo(
                tradeName = "Seclo (সেকলো)",
                genericName = "Omeprazole (ওমিপ্রাজল) ২০ মি.গ্রা.",
                indications = "গ্যাস্ট্রোইসোফেজিয়াল রিফ্লাক্স (GERD), পেপটিক আলসার, বুক জ্বালাপোড়া ও গ্যাস্ট্রিক সমস্যা।",
                dosageAdult = "২০ মি.গ্রা. দৈনিক একবার খাবারের ৩০ মিনিট পূর্বে। প্রয়োজনে ৪০ মি.গ্রা.",
                sideEffects = "মাথা ব্যথা, পেট খারাপ, কোষ্ঠকাঠিন্য, বমি বমি ভাব।",
                interactions = "ক্লপিডোগ্রেল ও কেটোকোনাজোলের কার্যকারিতা কমাতে পারে।",
                warnings = "দীর্ঘমেয়াদী ব্যবহারে ভিটামিন বি১২ ও ম্যাগনেসিয়াম স্বল্পতার ঝুঁকি থাকে।"
            ),
            MedicineInfo(
                tradeName = "Fexo (ফেক্সো)",
                genericName = "Fexofenadine (ফেক্সোফেনাডিন) ১২০/১৮০ মি.গ্রা.",
                indications = "অ্যালার্জিক রাইনাইটিস, হাঁচি, চুলকানি, নাক দিয়ে পানি পড়া, আর্টিকেরিয়া।",
                dosageAdult = "১২০ মি.গ্রা. দৈনিক একবার অথবা ৬০ মি.গ্রা. দৈনিক দুইবার।",
                sideEffects = "ক্লান্তি, মাথা ঘোরা, মৃদু তন্দ্রাভাব (সাধারণ অ্যান্টিহিস্টামিনের চেয়ে অনেক কম)।",
                interactions = "অ্যালুমিনিয়াম বা ম্যাগনেসিয়ামযুক্ত এন্টাসিডের সাথে গ্রহণে শোষণ কমে যায়।",
                warnings = "বৃক্কের জটিলতায় সতর্ক ডোজ প্রয়োজন।"
            ),
            MedicineInfo(
                tradeName = "Azithromycin (অ্যাজিথ্রোমাইসিন)",
                genericName = "Azithromycin ৫০০ মি.গ্রা.",
                indications = "শ্বাসতন্ত্রের সংক্রমণ, টনসিলাইটিস, সাইনোসাইটিস, ত্বক ও কানের ব্যাকটেরিয়া সংক্রমণ।",
                dosageAdult = "৫০০ মি.গ্রা. দৈনিক একবার খাবারের ১ ঘণ্টা আগে বা ২ ঘণ্টা পরে, সাধারণত ৩-৫ দিন।",
                sideEffects = "পেট ব্যথা, ডায়রিয়া, বমি বমি ভাব, পেট ফাঁপা।",
                interactions = "অ্যান্টাসিড এর সাথে অবিলম্বে গ্রহণ অনুচিত। কিউটি ইন্টারভাল দীর্ঘায়িত করতে পারে।",
                warnings = "সম্পূর্ণ অ্যান্টিবায়োটিক কোর্স শেষ করুন। চিকিৎসকের প্রেসক্রিপশন ব্যতীত নিষিদ্ধ।"
            ),
            MedicineInfo(
                tradeName = "Pantonix (প্যান্টোনিক্স)",
                genericName = "Pantoprazole (প্যান্টোপ্রাজল) ২০/৪০ মি.গ্রা.",
                indications = "অ্যাসিড রিফ্লাক্স, খাদ্যনালীর প্রদাহ, আলসার রোগ নিরাময় ও প্রতিরোধ।",
                dosageAdult = "২০-৪০ মি.গ্রা. দৈনিক একবার সকালে খালি পেটে।",
                sideEffects = "মৃদু পেট ব্যথা, ডায়রিয়া, শুষ্ক মুখ, মাথা ঘোরা।",
                interactions = "মিথোট্রেক্সেট ও প্রোটিজ ইনহিবিটরের সাথে মিথস্ক্রিয়া হতে পারে।",
                warnings = "চিকিৎসকের পরামর্শ অনুযায়ী নির্দিষ্ট মেয়াদের জন্য সেবনযোগ্য।"
            )
        )
    }
}
