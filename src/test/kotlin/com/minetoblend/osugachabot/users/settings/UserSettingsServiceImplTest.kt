package com.minetoblend.osugachabot.users.settings

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class UserSettingsServiceImplTest {

    @Autowired
    private lateinit var userSettingsService: UserSettingsService

    @Test
    fun `reminders defaults to false for new users`() {
        val userId = UserId(1001L)

        val settings = userSettingsService.getSettings(userId)

        assertFalse(settings.reminders)
    }

    @Test
    fun `setReminders persists the value`() {
        val userId = UserId(1002L)

        userSettingsService.setReminders(userId, false)

        assertFalse(userSettingsService.getSettings(userId).reminders)
    }

    @Test
    fun `setReminders can toggle the value`() {
        val userId = UserId(1003L)

        userSettingsService.setReminders(userId, false)
        userSettingsService.setReminders(userId, true)

        assertTrue(userSettingsService.getSettings(userId).reminders)
    }
}
