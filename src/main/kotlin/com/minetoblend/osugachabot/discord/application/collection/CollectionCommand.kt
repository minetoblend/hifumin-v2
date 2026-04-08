package com.minetoblend.osugachabot.discord.application.collection

import com.minetoblend.osugachabot.cards.CardReplicaService
import com.minetoblend.osugachabot.cards.burnValue
import com.minetoblend.osugachabot.cards.icon
import com.minetoblend.osugachabot.discord.PaginatedMessage
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.entity.User
import dev.kord.core.entity.effectiveName
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.user
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.annotation.Order
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Order(4)
@Component
class CollectionCommand(
    @Qualifier("discordScope") private val scope: CoroutineScope,
    private val replicaService: CardReplicaService,
) : SlashCommand {
    override val name = "collection"
    override val description = "Shows your collection"

    override fun ChatInputCreateBuilder.declare() {
        user("user", "User to view the inventory of") {
            required = false
        }
        string("sort", "Sort order") {
            required = false
            choice("By date (newest first)", "date")
            choice("By rarity", "rarity")
            choice("By value", "value")
            choice("By name", "name")
            choice("By condition", "condition")
        }
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val user = interaction.command.users["user"] ?: interaction.user
        val sortOption = interaction.command.strings["sort"]

        scope.launch {
            CollectionMessage(user, interaction, sortOption).run(1.minutes)
        }
    }

    private inner class CollectionMessage(
        val user: User,
        interaction: ChatInputCommandInteraction,
        sortOption: String?,
    ) : PaginatedMessage(scope, interaction) {
        override val sort: Sort = when (sortOption) {
            "rarity"    -> Sort.by(Sort.Direction.DESC, "card.followerCount")
            "value"     -> Sort.by(Sort.Direction.DESC, "burnValue")
            "name"      -> Sort.by(Sort.Direction.ASC, "card.username")
            "condition" -> Sort.by(Sort.Direction.ASC, "condition")
            else        -> Sort.by(Sort.Direction.DESC, "createdAt")
        }

        override suspend fun getItemCount(): Int = replicaService.getCardCount(user.id.toUserId())

        override suspend fun MessageBuilder.renderPage(page: PageRequest) {
            val replicas = replicaService.findByUserId(user.id.toUserId(), page)

            embed {
                title = "Card collection for ${user.effectiveName}"

                description = when (pageCount) {
                    0 -> "${user.effectiveName} has no cards"
                    else -> replicas.joinToString("\n") { replica ->
                        "`${replica.id.toDisplayId()}` · ${replica.condition.icon} · ${replica.card.username} (${replica.card.rarity}, ${replica.burnValue} gold)"
                    }
                }

                pageFooter()
            }
        }
    }
}