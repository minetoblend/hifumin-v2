package com.minetoblend.osugachabot.discord.application.trade

import com.minetoblend.osugachabot.cards.CardReplicaId.Companion.toCardReplicaIdOrNull
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.interactionButton
import com.minetoblend.osugachabot.discord.utils.cardId
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.graphics.toRenderableCard
import com.minetoblend.osugachabot.trading.TradeService
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.actionRow
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.user
import dev.kord.rest.builder.message.embed
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class TradeCommand(
    private val tradeService: TradeService,
    private val cardRenderer: CardRenderer,
) : SlashCommand {
    override val name = "trade"
    override val description = "Propose a card trade with another player"

    override fun ChatInputCreateBuilder.declare() {
        user("player", "The player you want to trade with") { required = true }
        cardId("offer", required = true, description = "The card you want to offer")
        cardId("for", required = true, description = "The card you want in return")
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val targetUser = interaction.command.users["player"]!!
        val offeredId = interaction.command.strings["offer"]?.toCardReplicaIdOrNull()
        val requestedId = interaction.command.strings["for"]?.toCardReplicaIdOrNull()

        if (offeredId == null || requestedId == null) {
            interaction.respondEphemeral { content = "Invalid card ID format." }
            return
        }

        val initiatorUserId = interaction.user.id.toUserId()
        val targetUserId = targetUser.id.toUserId()

        when (val result = tradeService.createTrade(initiatorUserId, targetUserId, offeredId, requestedId)) {
            is Created -> {
                val trade = result.trade

                coroutineScope {
                    val cardImage = async {
                        cardRenderer.renderCards(listOf(
                            result.offeredCard.toRenderableCard(),
                            result.requestedCard.toRenderableCard(),
                        ))
                    }

                    val ack = interaction.deferPublicResponse()
                    val renderedImage = cardImage.await()

                    ack.respond {
                        content = "${targetUser.mention}, ${interaction.user.mention} wants to trade with you!"
                        addFile(
                            name = "trade.png",
                            contentProvider = ChannelProvider { ByteReadChannel(renderedImage) }
                        )
                        embed {
                            title = "Trade Offer"
                            description = buildString {
                                appendLine("${interaction.user.mention} wants to trade with ${targetUser.mention}!")
                                appendLine()
                                appendLine("**Offering:** ${result.offeredCard.card.username} (`${trade.offeredCardId.toDisplayId()}`)")
                                appendLine("**Wants:** ${result.requestedCard.card.username} (`${trade.requestedCardId.toDisplayId()}`)")
                            }
                            image = "attachment://trade.png"
                        }
                        actionRow {
                            interactionButton(
                                ButtonStyle.Success,
                                TradeButtonId(trade.id, TradeButtonId.Action.Accept)
                            ) {
                                label = "Accept"
                            }
                            interactionButton(
                                ButtonStyle.Danger,
                                TradeButtonId(trade.id, TradeButtonId.Action.Decline)
                            ) {
                                label = "Decline"
                            }
                            interactionButton(
                                ButtonStyle.Secondary,
                                TradeButtonId(trade.id, TradeButtonId.Action.Cancel)
                            ) {
                                label = "Cancel"
                            }
                        }
                    }
                }
            }

            CannotTradeWithSelf ->
                interaction.respondEphemeral { content = "You can't trade with yourself!" }

            OfferedCardNotFound ->
                interaction.respondEphemeral { content = "The card you want to offer was not found." }

            OfferedCardNotOwned ->
                interaction.respondEphemeral { content = "You don't own that card!" }

            RequestedCardNotFound ->
                interaction.respondEphemeral { content = "The card you want in return was not found." }

            RequestedCardNotOwned ->
                interaction.respondEphemeral { content = "That player doesn't own the card you're asking for." }

            is OfferedCardLocked ->
                interaction.respondEphemeral { content = result.reason }

            is RequestedCardLocked ->
                interaction.respondEphemeral { content = result.reason }
        }
    }
}
