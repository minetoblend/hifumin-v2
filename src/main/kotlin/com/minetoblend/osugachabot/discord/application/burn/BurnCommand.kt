package com.minetoblend.osugachabot.discord.application.burn

import com.minetoblend.osugachabot.cards.CardReplicaId.Companion.toCardReplicaIdOrNull
import com.minetoblend.osugachabot.cards.CardReplicaService
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.utils.cardId
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import kotlinx.coroutines.CoroutineScope
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(3)
@Component
class BurnCommand(
    private val cardReplicaService: CardReplicaService,
    private val cardRenderer: CardRenderer,
    @Qualifier("discordScope") private val scope: CoroutineScope,
) :
    SlashCommand {
    override val name = "burn"
    override val description = "Burn a card"

    override fun ChatInputCreateBuilder.declare() {
        cardId("id", required = false)
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val cardId = interaction.command.strings["id"]?.toCardReplicaIdOrNull()

        when (val result = cardReplicaService.findOwnedCardOrLatest(cardId, interaction.user.id.toUserId())) {
            NotFound -> interaction.respondEphemeral { content = "You have no cards to burn!" }
            NotOwned -> interaction.respondEphemeral { content = "This is not your card!" }
            is Success -> {
                BurnDialog(interaction, result.replica, cardReplicaService, cardRenderer, scope).run()
            }
        }
    }
}