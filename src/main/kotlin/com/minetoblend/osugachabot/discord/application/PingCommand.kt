package com.minetoblend.osugachabot.discord.application

import com.minetoblend.osugachabot.discord.SlashCommand
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import kotlin.time.measureTimedValue
import org.springframework.stereotype.Component

@Component
class PingCommand : SlashCommand {
    override val name = "ping"
    override val description = "Check the bot's latency"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val (response, ping) = measureTimedValue {
            interaction.respondPublic {
                content = "Pong!"
            }
        }
        response.edit {
            content = "Pong! (${ping.inWholeMilliseconds}ms)"
        }
    }
}