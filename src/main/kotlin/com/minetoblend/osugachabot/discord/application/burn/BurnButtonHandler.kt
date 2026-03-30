package com.minetoblend.osugachabot.discord.application.burn

import com.minetoblend.osugachabot.cards.CardReplicaService
import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import kotlinx.coroutines.CoroutineScope
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class BurnButtonHandler(
    private val cardReplicaService: CardReplicaService,
    private val cardRenderer: CardRenderer,
    @Qualifier("discordScope") private val scope: CoroutineScope,
) : ButtonInteractionHandler {

    override fun canHandle(customId: String) = BurnButtonId.isValid(customId)

    override suspend fun ButtonInteractionCreateEvent.handle() {
        val buttonId = BurnButtonId.fromString(interaction.componentId) ?: return
        val userId = interaction.user.id.toUserId()

        val replica = cardReplicaService.findById(buttonId.replicaId)
        if (replica == null) {
            interaction.respondEphemeral { content = "Card not found!" }
            return
        }
        if (replica.userId != userId) {
            interaction.respondEphemeral { content = "This is not your card!" }
            return
        }

        BurnDialog(interaction, replica, cardReplicaService, cardRenderer, scope).run()
    }
}
