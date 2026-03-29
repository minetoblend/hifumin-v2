package com.minetoblend.osugachabot.discord.application.view

import com.minetoblend.osugachabot.cards.CardService
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.graphics.CardRenderer
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class CardCommand(private val cardService: CardService, private val cardRenderer: CardRenderer) : SlashCommand {
    override val name = "card"
    override val description = "Display a card"

    override fun ChatInputCreateBuilder.declare() {
        string("username", "Username of the card") {
            required = true
        }
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val username = interaction.command.strings["username"]!!

        val card = cardService.findByUsername(username)

        if (card == null) {
            interaction.respondPublic { content = "No card found" }
            return
        }



        coroutineScope {
            val cardImageAsync = async { cardRenderer.renderCard(card) }

            val ack = interaction.deferPublicResponse()

            val cardImage = cardImageAsync.await()

            ack.respond {
                addFile(
                    name = "card.png",
                    contentProvider = ChannelProvider { ByteReadChannel(cardImage) }
                )

                embed {
                    title = card.username
                    url = "https://osu.ppy.sh/users/${card.userId}"

                    field {
                        name = "Rarity"
                        value = card.rarity.toString()
                    }

                    if (card.globalRank != null) {
                        field {
                            name = "Global Rank"
                            value = card.globalRank.toString()
                        }
                    }

                    field {
                        name = "Follower Count"
                        value = card.followerCount.toString()
                    }

                    image = "attachment://card.png"
                }
            }
        }


    }
}