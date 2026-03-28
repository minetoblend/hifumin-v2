package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.drops.ClaimResult
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.users.UserId
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.message.actionRow
import org.springframework.stereotype.Component

@Component
class ClaimButtonHandler(private val dropService: DropService) : ButtonInteractionHandler {
    override fun canHandle(customId: String) = ClaimButtonId.isValid(customId)

    override suspend fun ButtonInteractionCreateEvent.handle() {
        val buttonId = ClaimButtonId.fromString(interaction.componentId) ?: return

        val dropId = buttonId.dropId
        val cardIndex = buttonId.cardIndex
        val userId = UserId(interaction.user.id.value.toLong())

        val ack = interaction.deferPublicMessageUpdate()

        val drop = when (val result = dropService.claimCard(dropId, cardIndex, userId)) {
            is ClaimResult.Claimed -> result.drop
            is ClaimResult.AlreadyClaimed -> result.drop
            ClaimResult.DropNotFound -> return
        }

        ack.edit {
            components = mutableListOf()
            actionRow {
                drop.cards.forEach { droppedCard ->
                    dropCardButton(drop, droppedCard)
                }
            }
        }
    }
}
