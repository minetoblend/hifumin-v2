package com.minetoblend.osugachabot.drops

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.discord.DiscordMessagingService
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.settings.UserSettingsService
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class DropReminderServiceTest {

    @TestConfiguration
    class Config {
        @Bean
        @Primary
        fun recordingDiscordMessagingService() = RecordingDiscordMessagingService()
    }

    @Autowired
    private lateinit var dropReminderService: DropReminderService

    @Autowired
    private lateinit var userSettingsService: UserSettingsService

    @Autowired
    private lateinit var recordingMessagingService: RecordingDiscordMessagingService

    @Test
    fun `sendReminderIfEnabled sends DM when reminders are enabled`() = runBlocking {
        val userId = UserId(3001L)
        userSettingsService.setReminders(userId, true)
        recordingMessagingService.clear()

        dropReminderService.sendReminderIfEnabled(userId)

        assertTrue(recordingMessagingService.wasSentTo(userId))
    }

    @Test
    fun `sendReminderIfEnabled does not send DM when reminders are disabled`() = runBlocking {
        val userId = UserId(3002L)
        userSettingsService.setReminders(userId, false)
        recordingMessagingService.clear()

        dropReminderService.sendReminderIfEnabled(userId)

        assertFalse(recordingMessagingService.wasSentTo(userId))
    }

    @Test
    fun `sendReminderIfEnabled does not send DM when reminders default to false`() = runBlocking {
        val userId = UserId(3003L)
        recordingMessagingService.clear()

        dropReminderService.sendReminderIfEnabled(userId)

        assertFalse(recordingMessagingService.wasSentTo(userId))
    }
}

class RecordingDiscordMessagingService : DiscordMessagingService {
    private val recipients = mutableListOf<UserId>()

    override suspend fun sendDm(userId: UserId, message: String) {
        recipients.add(userId)
    }

    fun wasSentTo(userId: UserId) = recipients.contains(userId)

    fun clear() = recipients.clear()
}
