package com.minetoblend.osugachabot.discord.application.cooldown

import com.minetoblend.osugachabot.cooldown.CooldownResult
import com.minetoblend.osugachabot.cooldown.CooldownService
import com.minetoblend.osugachabot.cooldown.CooldownType
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.users.UserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import org.springframework.stereotype.Component
import kotlin.time.Clock
import kotlin.time.Duration

private fun Duration.toDiscordRelativeTimestamp(): String {
    val epochSeconds = (Clock.System.now() + this).epochSeconds
    return "<t:$epochSeconds:R>"
}

@Component
class CooldownCommand(
    private val cooldownService: CooldownService,
) : SlashCommand {
    override val name = "cooldown"
    override val description = "Show your current cooldowns"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val userId = UserId(interaction.user.id.value.toLong())

        val lines = CooldownType.entries.map { type ->
            val label = type.value.replaceFirstChar { it.uppercaseChar() }
            when (val result = cooldownService.checkCooldown(userId, type)) {
                is CooldownResult.Ready -> "**$label**: Ready"
                is CooldownResult.OnCooldown -> "**$label**: ${result.remaining.toDiscordRelativeTimestamp()}"
            }
        }

        interaction.respondEphemeral {
            content = lines.joinToString("\n")
        }
    }
}
