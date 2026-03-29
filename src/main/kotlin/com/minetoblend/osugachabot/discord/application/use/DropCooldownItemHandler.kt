package com.minetoblend.osugachabot.discord.application.use

import com.minetoblend.osugachabot.discord.ConsumableItemHandler
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.statuseffect.StatusEffect
import com.minetoblend.osugachabot.statuseffect.StatusEffectService
import com.minetoblend.osugachabot.users.UserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import org.springframework.stereotype.Component
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

@Component
class DropCooldownItemHandler(
    private val statusEffectService: StatusEffectService,
) : ConsumableItemHandler {

    override val itemType = ItemType.DropSpeedup

    override suspend fun ChatInputCommandInteractionCreateEvent.handle(userId: UserId) {
        val result = statusEffectService.applyEffect(userId, DropCooldownReduction, 6.hours)

        val duration = (result.expiresAt - Clock.System.now())

        interaction.respondPublic {
            content = "${interaction.user.mention} Your drop cooldown is reduced by **50%** for the next **${duration.inWholeHours} hours**!"
        }
    }
}
