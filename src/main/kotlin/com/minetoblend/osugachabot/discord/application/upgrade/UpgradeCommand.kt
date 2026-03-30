package com.minetoblend.osugachabot.discord.application.upgrade

import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.cards.CardReplicaId.Companion.toCardReplicaIdOrNull
import com.minetoblend.osugachabot.cards.CardReplicaService
import com.minetoblend.osugachabot.cards.OwnedCardResult.NotFound
import com.minetoblend.osugachabot.cards.OwnedCardResult.NotOwned
import com.minetoblend.osugachabot.cards.OwnedCardResult.Success
import com.minetoblend.osugachabot.cards.UpgradeCardResult
import com.minetoblend.osugachabot.cards.icon
import com.minetoblend.osugachabot.cards.nextCondition
import com.minetoblend.osugachabot.cards.prettyName
import com.minetoblend.osugachabot.cards.upgradeCost
import com.minetoblend.osugachabot.cards.upgradeSuccessRate
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.utils.cardId
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.response.PublicMessageInteractionResponse
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.actionRow
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.embed
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

@Component
class UpgradeCommand(
    private val cardReplicaService: CardReplicaService,
    private val cardRenderer: CardRenderer,
    @Qualifier("discordScope") private val scope: CoroutineScope,
) : SlashCommand {
    override val name = "upgrade"
    override val description = "Attempt to upgrade a card's condition"

    override fun ChatInputCreateBuilder.declare() {
        cardId("id", required = false)
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val cardId = interaction.command.strings["id"]?.toCardReplicaIdOrNull()

        when (val result = cardReplicaService.findOwnedCardOrLatest(cardId, interaction.user.id.toUserId())) {
            NotFound -> interaction.respondEphemeral { content = "You have no cards to upgrade!" }
            NotOwned -> interaction.respondEphemeral { content = "This is not your card!" }
            is Success -> {
                if (result.replica.condition == CardCondition.Mint) {
                    interaction.respondEphemeral { content = "This card is already in mint condition!" }
                    return
                }
                UpgradeDialog(interaction, result.replica).run()
            }
        }
    }

    inner class UpgradeDialog(
        val interaction: ChatInputCommandInteraction,
        val replica: CardReplica,
    ) {
        private var status: UpgradeStatus = UpgradeStatus.Pending
        private var upgradeResult: UpgradeCardResult? = null

        private val targetCondition = replica.condition.nextCondition()
        private val cost = replica.condition.upgradeCost
        private val successPercent = (replica.condition.upgradeSuccessRate * 100).roundToInt()

        fun MessageBuilder.render() {
            embed {
                title = "Upgrade Card"

                description = buildString {
                    when (status) {
                        UpgradeStatus.Pending -> {
                            appendLine("${interaction.user.mention} upgrade **${replica.card.username}** `${replica.id.toDisplayId()}`?")
                            appendLine()
                            appendLine("${replica.condition.icon} ${replica.condition.prettyName()} → ${targetCondition.icon} ${targetCondition.prettyName()}")
                            appendLine()
                            appendLine(":game_die: Success chance: **$successPercent%**")
                            appendLine(":money_bag: Cost: **$cost gold**")
                        }

                        UpgradeStatus.Succeeded -> {
                            val result = upgradeResult as UpgradeCardResult.Success
                            appendLine("${interaction.user.mention} the upgrade succeeded!")
                            appendLine()
                            appendLine("Your card is now **${result.newCondition.prettyName()}** ${result.newCondition.icon}")
                        }

                        UpgradeStatus.Failed -> {
                            val result = upgradeResult as UpgradeCardResult.Failed
                            appendLine("${interaction.user.mention} the upgrade failed.")
                            appendLine()
                            appendLine("Your card remains **${result.condition.prettyName()}** ${result.condition.icon}")
                            appendLine(":money_bag: $cost gold was spent.")
                        }

                        UpgradeStatus.Cancelled -> {
                            appendLine("Upgrade cancelled.")
                        }
                    }
                }

                color = when (status) {
                    UpgradeStatus.Pending -> null
                    UpgradeStatus.Succeeded -> Color(0, 200, 80)
                    UpgradeStatus.Failed -> Color(200, 80, 0)
                    UpgradeStatus.Cancelled -> Color(100, 100, 100)
                }

                thumbnail {
                    url = "attachment://card.png"
                }

                field {
                    name = "Rarity"
                    value = replica.card.rarity.name
                    inline = true
                }

                field {
                    name = "Condition"
                    value = replica.condition.prettyName()
                    inline = true
                }
            }

            components = mutableListOf()
            if (status == UpgradeStatus.Pending) {
                actionRow {
                    interactionButton(ButtonStyle.Primary, BUTTON_CONFIRM) {
                        label = "Upgrade ($cost gold)"
                    }
                    interactionButton(ButtonStyle.Secondary, BUTTON_CANCEL) {
                        label = "Cancel"
                    }
                }
            }
        }

        suspend fun run() {
            val response = coroutineScope {
                val cardImage = async { cardRenderer.renderCard(replica.card, replica.foil) }

                val ack = interaction.deferPublicResponse()

                val image = cardImage.await()

                ack.respond {
                    addFile(
                        name = "card.png",
                        contentProvider = ChannelProvider { ByteReadChannel(image) }
                    )
                    render()
                }
            }

            scope.launch(Context.root().asContextElement()) {
                val job = launch {
                    receiveButtonEvents(response)
                }

                delay(2.minutes)
                job.cancel()

                response.edit {
                    components = mutableListOf()
                }
            }
        }

        private fun receiveButtonEvents(response: PublicMessageInteractionResponse) {
            interaction.kord.on<ButtonInteractionCreateEvent> {
                if (interaction.message.id != response.message.id)
                    return@on

                if (interaction.user.id != this.interaction.user.id) {
                    interaction.respondEphemeral { content = "This is not your card!" }
                    return@on
                }

                when (interaction.componentId) {
                    BUTTON_CANCEL -> {
                        status = UpgradeStatus.Cancelled
                        interaction.updatePublicMessage { render() }
                    }

                    BUTTON_CONFIRM -> {
                        val result = cardReplicaService.upgradeCard(replica.id, interaction.user.id.toUserId())
                        upgradeResult = result

                        when (result) {
                            is UpgradeCardResult.Success -> {
                                status = UpgradeStatus.Succeeded
                                interaction.updatePublicMessage { render() }
                            }

                            is UpgradeCardResult.Failed -> {
                                status = UpgradeStatus.Failed
                                interaction.updatePublicMessage { render() }
                            }

                            UpgradeCardResult.InsufficientGold ->
                                interaction.respondEphemeral {
                                    content = "You don't have enough gold! You need $cost gold."
                                }

                            UpgradeCardResult.AlreadyMint ->
                                interaction.respondEphemeral {
                                    content = "This card is already in mint condition!"
                                }

                            UpgradeCardResult.NotFound ->
                                interaction.respondEphemeral { content = "Card not found." }

                            UpgradeCardResult.NotOwned ->
                                interaction.respondEphemeral { content = "This is not your card!" }
                        }
                    }
                }
            }
        }
    }

    enum class UpgradeStatus {
        Pending,
        Succeeded,
        Failed,
        Cancelled,
    }

    companion object {
        const val BUTTON_CONFIRM = "card:upgrade:confirm"
        const val BUTTON_CANCEL = "card:upgrade:cancel"
    }
}
