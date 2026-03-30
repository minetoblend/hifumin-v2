package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.interactionButton
import com.minetoblend.osugachabot.drops.CreateDropResult
import com.minetoblend.osugachabot.drops.Drop
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.drops.DroppedCard
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.graphics.toRenderableCard
import com.minetoblend.osugachabot.users.UserId
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.actionRow
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import kotlin.time.Clock
import kotlin.time.Duration

private fun Duration.toDiscordRelativeTimestamp(): String {
    val epochSeconds = (Clock.System.now() + this).epochSeconds
    return "<t:$epochSeconds:R>"
}

@Component
class DropCommand(
    private val dropService: DropService,
    @Qualifier("discordScope") private val scope: CoroutineScope,
    private val cardRenderer: CardRenderer,
) : SlashCommand {
    override val name = "drop"
    override val description = "Drop 3 cards for players to claim"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val userId = UserId(interaction.user.id.value.toLong())
        when (val result = dropService.createDrop(userId)) {
            is CreateDropResult.OnCooldown -> {
                interaction.respondEphemeral {
                    content = "Drops are on cooldown! Try again ${result.remaining.toDiscordRelativeTimestamp()}."
                }
            }

            is CreateDropResult.Created -> {
                val drop = result.drop
                val ack = interaction.deferPublicResponse()

                val image = cardRenderer.renderCards(drop.cards.map { it.toRenderableCard() })

                val response = ack.respond {


                    addFile(
                        name = "cards.png",
                        contentProvider = ChannelProvider { ByteReadChannel(image) }
                    )

                    content = "${interaction.user.mention} Dropping 3 cards..."

                    actionRow {
                        drop.cards.forEach { droppedCard ->
                            dropCardButton(drop, droppedCard)
                        }
                    }
                }

                scope.launch(Context.root().asContextElement()) {
                    delay(dropService.dropExpiryDuration())
                    response.edit {
                        components = mutableListOf()
                    }
                }

            }
        }
    }
}

internal fun ActionRowBuilder.dropCardButton(drop: Drop, droppedCard: DroppedCard) {
    val foilPrefix = if (droppedCard.foil) "✨ " else ""
    if (droppedCard.claimedBy == null) {
        interactionButton(ButtonStyle.Primary, ClaimButtonId(drop.id, droppedCard.index)) {
            label = "$foilPrefix${droppedCard.card.username}"
        }
    } else {
        interactionButton(ButtonStyle.Secondary, ClaimButtonId(drop.id, droppedCard.index)) {
            label = "Claimed: $foilPrefix${droppedCard.card.username}"
            disabled = true
        }
    }
}