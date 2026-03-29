package com.minetoblend.osugachabot.discord.application.daily

import com.minetoblend.osugachabot.cooldown.CooldownResult
import com.minetoblend.osugachabot.cooldown.CooldownService
import com.minetoblend.osugachabot.cooldown.CooldownType
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import org.springframework.stereotype.Component
import kotlin.time.Clock
import kotlin.time.Duration

private const val DAILY_GOLD_REWARD = 100L

private fun Duration.toDiscordRelativeTimestamp(): String {
    val epochSeconds = (Clock.System.now() + this).epochSeconds
    return "<t:$epochSeconds:R>"
}

@Component
class DailyCommand(
    private val cooldownService: CooldownService,
    private val inventoryService: InventoryService,
) : SlashCommand {
    override val name = "daily"
    override val description = "Claim your daily gold reward"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val userId = interaction.user.id.toUserId()

        when (val result = cooldownService.tryConsume(userId, CooldownType.DAILY)) {
            is CooldownResult.Ready -> {
                inventoryService.addItems(userId, ItemType.Gold, DAILY_GOLD_REWARD)
                interaction.respondPublic {
                    content = "${interaction.user.mention} you claimed your daily reward: :money_bag: **$DAILY_GOLD_REWARD gold**!"
                }
            }
            is CooldownResult.OnCooldown -> {
                interaction.respondEphemeral {
                    content = "You already claimed your daily reward. Come back ${result.remaining.toDiscordRelativeTimestamp()}!"
                }
            }
        }
    }
}
