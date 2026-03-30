package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.discord.ConsumableItemHandler
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.graphics.toRenderableCard
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.users.UserId
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.actionRow
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.utils.io.ByteReadChannel
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class SuperDropItemHandler(
    private val dropService: DropService,
    private val cardRenderer: CardRenderer
) : ConsumableItemHandler {
    override val itemType: ItemType = SuperDrop

    override suspend fun ChatInputCommandInteractionCreateEvent.handle(userId: UserId) {
        val drop = dropService.createSuperDrop(userId)

        val ack = interaction.deferPublicResponse()

        val image = cardRenderer.renderCards(drop.cards.map { it.toRenderableCard() })

        val response = ack.respond {
            addFile(
                name = "cards.png",
                contentProvider = ChannelProvider { ByteReadChannel(image) }
            )

            content = "${interaction.user.mention} Dropping ${drop.cards.size} cards..."

            drop.cards.chunked(5).forEach { chunk ->
                actionRow {
                    chunk.forEach { droppedCard ->
                        dropCardButton(drop, droppedCard)
                    }
                }
            }
        }

        kord.launch(Context.root().asContextElement()) {
            delay(dropService.dropExpiryDuration())
            response.edit {
                components = mutableListOf()
            }
        }
    }
}