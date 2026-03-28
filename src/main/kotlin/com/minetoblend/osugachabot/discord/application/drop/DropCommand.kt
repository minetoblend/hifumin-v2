package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.interactionButton
import com.minetoblend.osugachabot.drops.Drop
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.drops.DroppedCard
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.actionRow
import org.springframework.stereotype.Component


@Component
class DropCommand(private val dropService: DropService) : SlashCommand {
    override val name = "drop"
    override val description = "Drop 3 cards for players to claim"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val drop = dropService.createDrop()

        interaction.respondPublic {
            content = "${interaction.user.mention} Dropping 3 cards..."
            actionRow {
                drop.cards.forEach { droppedCard ->
                    dropCardButton(drop, droppedCard)
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