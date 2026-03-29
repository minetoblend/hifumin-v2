package com.minetoblend.osugachabot.discord.application.collection

import com.minetoblend.osugachabot.cards.CardReplicaService
import com.minetoblend.osugachabot.cards.burnValue
import com.minetoblend.osugachabot.discord.PaginatedMessage
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.entity.effectiveName
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class CollectionCommand(
    @Qualifier("discordScope") private val scope: CoroutineScope,
    private val replicaService: CardReplicaService,
) : SlashCommand {
    override val name = "collection"
    override val description = "Shows your collection"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        scope.launch {
            CollectionMessage(interaction).run(1.minutes)
        }
    }

    private inner class CollectionMessage(interaction: ChatInputCommandInteraction) :
        PaginatedMessage(scope, interaction) {
        override suspend fun getItemCount(): Int = replicaService.getCardCount(interaction.user.id.toUserId())

        override suspend fun MessageBuilder.renderPage(page: PageRequest) {
            val replicas = replicaService.findByUserId(interaction.user.id.toUserId(), page)

            embed {
                title = "Card collection for ${interaction.user.effectiveName}"

                description = replicas.joinToString("\n") { replica ->
                    "`${replica.id.toDisplayId()}` · ${replica.condition} · ${replica.card.username} (${replica.burnValue} gold)"
                }

                pageFooter()
            }
        }
    }
}