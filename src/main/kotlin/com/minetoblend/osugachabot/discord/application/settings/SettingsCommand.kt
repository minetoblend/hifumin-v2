package com.minetoblend.osugachabot.discord.application.settings

import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.settings.UserSettingsService
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.subCommand
import org.springframework.stereotype.Component

@Component
class SettingsCommand(
    private val userSettingsService: UserSettingsService,
) : SlashCommand {
    override val name = "settings"
    override val description = "Manage your personal bot settings"

    override fun ChatInputCreateBuilder.declare() {
        subCommand("reminders", "Enable or disable drop reminders") {
            boolean("enabled", "Whether to receive drop reminders") {
                required = true
            }
        }
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val userId = UserId(interaction.user.id.value.toLong())

        when (val cmd = interaction.command) {
            is SubCommand -> when (cmd.name) {
                "reminders" -> {
                    val enabled = cmd.booleans["enabled"]!!
                    userSettingsService.setReminders(userId, enabled)
                    val status = if (enabled) "enabled" else "disabled"
                    interaction.respondEphemeral {
                        content = "Reminders $status."
                    }
                }
            }

            else -> {}
        }
    }
}
