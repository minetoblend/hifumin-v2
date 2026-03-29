package com.minetoblend.osugachabot.discord.application.stats

import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.stats.UserAction
import com.minetoblend.osugachabot.stats.UserStatsService
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import org.springframework.stereotype.Component

@Component
class StatsCommand(
    private val userStatsService: UserStatsService,
) : SlashCommand {
    override val name = "stats"
    override val description = "View your action stats"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val userId = interaction.user.id.toUserId()
        val stats = userStatsService.getStats(userId)

        interaction.respondPublic {
            embed {
                title = "Your Stats"
                description = UserAction.entries.joinToString("\n") { action ->
                    "**${action.label}**: ${stats[action] ?: 0}"
                }
            }
        }
    }
}
