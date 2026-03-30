package com.minetoblend.osugachabot.discord.application.view

import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.cards.CardReplicaService
import com.minetoblend.osugachabot.cards.prettyName
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.graphics.CardRenderer
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.utils.io.ByteReadChannel
import org.springframework.stereotype.Component

@Component
class ViewCommand(
    private val cardReplicaService: CardReplicaService,
    private val cardRenderer: CardRenderer,
) : SlashCommand {
    override val name = "view"
    override val description = "View a card by its ID"

    override fun ChatInputCreateBuilder.declare() {
        string("id", "The card ID (e.g. aaaa)") {
            required = true
        }
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val rawId = interaction.command.strings["id"]!!

        val replicaId = CardReplicaId.fromDisplayId(rawId)
        if (replicaId == null) {
            interaction.respondEphemeral { content = "Invalid card ID." }
            return
        }

        val replica = cardReplicaService.findById(replicaId)
        if (replica == null) {
            interaction.respondEphemeral { content = "Card `$rawId` not found." }
            return
        }

        val ack = interaction.deferPublicResponse()
        val cardImage = cardRenderer.renderCard(replica.card, replica.foil)

        ack.respond {
            addFile(
                name = "card.png",
                contentProvider = ChannelProvider { ByteReadChannel(cardImage) }
            )

            embed {
                title = "`${replica.id.toDisplayId()}` - ${replica.card.username}"

                field {
                    name = "Condition"
                    value = replica.condition.prettyName()
                }

                image = "attachment://card.png"
            }
        }
    }
}
