package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.interactionButton
import com.minetoblend.osugachabot.drops.CreateDropResult
import com.minetoblend.osugachabot.drops.Drop
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.drops.DroppedCard
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.actionRow
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import kotlin.math.ceil
import kotlin.time.Duration

private fun Duration.formatMinutesSeconds(): String {
    val totalSeconds = ceil(inWholeSeconds.toDouble()).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

@Component
class DropCommand(
    private val dropService: DropService,
    @Qualifier("discordScope") private val scope: CoroutineScope,
) : SlashCommand {
    override val name = "drop"
    override val description = "Drop 3 cards for players to claim"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        when (val result = dropService.createDrop()) {
            is CreateDropResult.OnCooldown -> {
                interaction.respondEphemeral {
                    content = "Drops are on cooldown! Try again in ${result.remaining.formatMinutesSeconds()}."
                }
            }
            is CreateDropResult.Created -> {
                val drop = result.drop
                val response = interaction.respondPublic {
                    content = "${interaction.user.mention} Dropping 3 cards..."
                    actionRow {
                        drop.cards.forEach { droppedCard ->
                            dropCardButton(drop, droppedCard)
                        }
                    }
                }

                scope.launch(Context.root().asContextElement()) {
                    delay(dropService.expiryDuration())
                    response.edit {
                        components = mutableListOf()
                    }
                }
            }
        }
    }
}

internal fun ActionRowBuilder.dropCardButton(drop: Drop, droppedCard: DroppedCard) {
    if (droppedCard.claimedBy == null) {
        interactionButton(ButtonStyle.Primary, ClaimButtonId(drop.id, droppedCard.index)) {
            label = droppedCard.card.username
        }
    } else {
        interactionButton(ButtonStyle.Secondary, ClaimButtonId(drop.id, droppedCard.index)) {
            label = "Claimed: ${droppedCard.card.username}"
            disabled = true
        }
    }
}