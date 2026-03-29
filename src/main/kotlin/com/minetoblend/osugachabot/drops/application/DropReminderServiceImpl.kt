package com.minetoblend.osugachabot.drops.application

import com.minetoblend.osugachabot.discord.DiscordMessagingService
import com.minetoblend.osugachabot.drops.DropReminderService
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.settings.UserSettingsService
import org.springframework.stereotype.Service

@Service
class DropReminderServiceImpl(
    private val userSettingsService: UserSettingsService,
    private val discordMessagingService: DiscordMessagingService,
) : DropReminderService {

    override suspend fun sendReminderIfEnabled(userId: UserId) {
        if (userSettingsService.getSettings(userId).reminders) {
            discordMessagingService.sendDm(userId, "Your drop is ready! Use `/drop` to drop cards.")
        }
    }
}
