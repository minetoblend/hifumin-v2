package com.minetoblend.osugachabot.discord.application.trade

import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.trading.*
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import org.springframework.stereotype.Component

@Component
class TradeButtonHandler(
    private val tradeService: TradeService,
) : ButtonInteractionHandler {

    override fun canHandle(customId: String) = TradeButtonId.isValid(customId)

    override suspend fun ButtonInteractionCreateEvent.handle() {
        val buttonId = TradeButtonId.fromString(interaction.componentId) ?: return
        val userId = interaction.user.id.toUserId()

        when (buttonId.action) {
            Accept -> handleAccept(buttonId.tradeId, userId)
            Decline -> handleDecline(buttonId.tradeId, userId)
            Cancel -> handleCancel(buttonId.tradeId, userId)
        }
    }

    private suspend fun ButtonInteractionCreateEvent.handleAccept(tradeId: TradeId, userId: com.minetoblend.osugachabot.users.UserId) {
        when (val result = tradeService.acceptTrade(tradeId, userId)) {
            is Accepted -> {
                interaction.updatePublicMessage {
                    embed {
                        title = "Trade Completed!"
                        description = buildString {
                            appendLine("The trade has been completed successfully!")
                            appendLine()
                            appendLine("**${result.offeredCard.card.username}** (`${result.offeredCard.id.toDisplayId()}`) → <@${result.trade.targetUserId.value}>")
                            appendLine("**${result.requestedCard.card.username}** (`${result.requestedCard.id.toDisplayId()}`) → <@${result.trade.initiatorUserId.value}>")
                        }
                        color = dev.kord.common.Color(0, 255, 0)
                    }
                    components = mutableListOf()
                    attachments = mutableListOf()
                }
            }

            TradeNotFound ->
                interaction.respondEphemeral { content = "This trade no longer exists." }

            NotTargetUser ->
                interaction.respondEphemeral { content = "This trade is not for you!" }

            TradeNoLongerValid ->
                interaction.respondEphemeral { content = "This trade is no longer valid." }

            CardNoLongerAvailable -> {
                interaction.updatePublicMessage {
                    embed {
                        title = "Trade Failed"
                        description = "One or both cards are no longer available for trade."
                        color = dev.kord.common.Color(255, 0, 0)
                    }
                    components = mutableListOf()
                    attachments = mutableListOf()
                }
            }
        }
    }

    private suspend fun ButtonInteractionCreateEvent.handleDecline(tradeId: TradeId, userId: com.minetoblend.osugachabot.users.UserId) {
        when (val result = tradeService.declineTrade(tradeId, userId)) {
            is Declined -> {
                interaction.updatePublicMessage {
                    embed {
                        title = "Trade Declined"
                        description = "<@${result.trade.targetUserId.value}> declined the trade."
                        color = dev.kord.common.Color(255, 0, 0)
                    }
                    components = mutableListOf()
                    attachments = mutableListOf()
                }
            }

            TradeNotFound ->
                interaction.respondEphemeral { content = "This trade no longer exists." }

            NotTargetUser ->
                interaction.respondEphemeral { content = "Only the target player can decline this trade." }

            TradeNoLongerValid ->
                interaction.respondEphemeral { content = "This trade is no longer valid." }
        }
    }

    private suspend fun ButtonInteractionCreateEvent.handleCancel(tradeId: TradeId, userId: com.minetoblend.osugachabot.users.UserId) {
        when (val result = tradeService.cancelTrade(tradeId, userId)) {
            is Cancelled -> {
                interaction.updatePublicMessage {
                    embed {
                        title = "Trade Cancelled"
                        description = "<@${result.trade.initiatorUserId.value}> cancelled the trade."
                        color = dev.kord.common.Color(255, 165, 0)
                    }
                    components = mutableListOf()
                    attachments = mutableListOf()
                }
            }

            TradeNotFound ->
                interaction.respondEphemeral { content = "This trade no longer exists." }

            NotInitiator ->
                interaction.respondEphemeral { content = "Only the trade initiator can cancel this trade." }

            TradeNoLongerValid ->
                interaction.respondEphemeral { content = "This trade is no longer valid." }
        }
    }
}
