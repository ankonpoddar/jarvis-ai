package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiService
import com.example.data.JarvisRepository
import com.example.model.ReminderItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS", appName)
    }

    @Test
    fun `test local bangla parser for alarm`() {
        val service = GeminiService()
        val result = service.parseCommandLocally("কাল সকাল ৭টায় এলার্ম দাও")
        assertEquals("ALARM_REMINDER", result.intent)
        assertTrue(result.spokenReply.isNotEmpty())
    }

    @Test
    fun `test local repository reminder storage`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = JarvisRepository(context)
        val initialCount = repo.getReminders().size

        repo.addReminder(
            ReminderItem(
                title = "টেস্ট রিমাইন্ডার",
                timeFormatted = "09:00 AM"
            )
        )

        assertEquals(initialCount + 1, repo.getReminders().size)
    }
}
