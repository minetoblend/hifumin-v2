package com.minetoblend.osugachabot.discord.application.burn

import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.cards.CardReplicaId.Companion.toCardReplicaIdOrNull
import com.minetoblend.osugachabot.cards.CardReplicaService
import com.minetoblend.osugachabot.cards.burnValue
import com.minetoblend.osugachabot.cards.prettyName
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.application.burn.BurnCommand.BurnStatus.Cancelled
import com.minetoblend.osugachabot.discord.application.burn.BurnCommand.BurnStatus.Confirmed
import com.minetoblend.osugachabot.discord.utils.cardId
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.response.PublicMessageInteractionResponse
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.component.actionRow
import dev.kord.rest.builder.message.embed
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

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
                BurnDialog(interaction, result.replica).run()
            }
        }
    }

    inner class BurnDialog(
        val interaction: ChatInputCommandInteraction,
        val replica: CardReplica,
    ) {

        private var status: BurnStatus = Pending

        fun MessageBuilder.render() {
            embed {
                title = "Burn Card"

                description = buildString {
                    appendLine("${interaction.user.mention} you will receive:")
                    appendLine()
                    appendLine(":money_bag: ${replica.burnValue} `gold`")
                }

                color = when (status) {
                    Pending -> null
                    Confirmed -> Color(0, 255, 0)
                    Cancelled -> Color(255, 0, 0)
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

                when (status) {
                    Cancelled -> footer {
                        text = "Burn has been cancelled"
                    }

                    Confirmed -> footer {
                        text = "Card has been burned"
                    }

                    else -> {}
                }
            }

            components = mutableListOf()
            if (status == Pending) {
                actionRow {
                    interactionButton(ButtonStyle.Primary, "card:burn:confirm") {
                        label = "Confirm"
                    }
                    interactionButton(ButtonStyle.Danger, "card:burn:cancel") {
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
                    "card:burn:cancel" -> {
                        status = Cancelled
                        interaction.updatePublicMessage { render() }
                    }

                    "card:burn:confirm" -> {
                        when (cardReplicaService.burnCard(replica.id, interaction.user.id.toUserId())) {
                            Success -> {
                                interaction.respondPublic {
                                    content = "${interaction.user.mention} you have burned your card!"
                                }

                                status = Confirmed
                                response.edit { render() }
                            }

                            NotFound -> interaction.respondEphemeral { content = "Card was not found!" }
                            NotOwned -> interaction.respondEphemeral { content = "This is not your card!" }
                        }
                    }
                }
            }
        }
    }

    enum class BurnStatus {
        Pending,
        Confirmed,
        Cancelled,
    }
}