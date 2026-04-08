package com.minetoblend.osugachabot.discord.application.leaderboard

import com.minetoblend.osugachabot.discord.PaginatedMessage
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.leaderboard.CollectionValueService
import com.minetoblend.osugachabot.stats.UserAction
import com.minetoblend.osugachabot.stats.UserStatsService
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class LeaderboardCommand(
    @Qualifier("discordScope") private val scope: CoroutineScope,
    private val collectionValueService: CollectionValueService,
    private val inventoryService: InventoryService,
    private val userStatsService: UserStatsService,
) : SlashCommand {
    override val name = "leaderboard"
    override val description = "Shows leaderboards"

    override fun ChatInputCreateBuilder.declare() {
        subCommand("collection", "Top collectors by total collection value") {}
        subCommand("gold", "Top users by gold in inventory") {}
        subCommand("claims", "Top users by cards claimed") {}
        subCommand("drops", "Top users by drops created") {}
        subCommand("burns", "Top users by cards burned") {}
        subCommand("mint", "Top users by mint condition cards owned") {}
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        when (val cmd = interaction.command) {
            is SubCommand -> when (cmd.name) {
                "collection" -> scope.launch {
                    CollectionValueLeaderboardMessage(interaction).run(1.minutes)
                }
                "gold" -> scope.launch {
                    GoldLeaderboardMessage(interaction).run(1.minutes)
                }
                "claims" -> scope.launch {
                    ClaimsLeaderboardMessage(interaction).run(1.minutes)
                }
                "drops" -> scope.launch {
                    DropsLeaderboardMessage(interaction).run(1.minutes)
                }
                "burns" -> scope.launch {
                    BurnsLeaderboardMessage(interaction).run(1.minutes)
                }
                "mint" -> scope.launch {
                    MintLeaderboardMessage(interaction).run(1.minutes)
                }
            }
            else -> {}
        }
    }

    private inner class CollectionValueLeaderboardMessage(interaction: ChatInputCommandInteraction) :
        PaginatedMessage(scope, interaction) {

        override suspend fun getItemCount(): Int =
            collectionValueService.getLeaderboard(PageRequest.of(0, 1)).totalElements.toInt()

        override suspend fun MessageBuilder.renderPage(page: PageRequest) {
            val entries = collectionValueService.getLeaderboard(page)
            val offset = page.pageNumber * page.pageSize

            embed {
                title = "Collection Value Leaderboard"

                description = when {
                    entries.isEmpty -> "No entries yet."
                    else -> entries.mapIndexed { i, entry ->
                        val rank = offset + i + 1
                        "**#$rank** ${entry.totalValue} gold · ${entry.cardCount} cards · <@${entry.userId.value}>"
                    }.joinToString("\n")
                }

                pageFooter()
            }
        }
    }

    private inner class GoldLeaderboardMessage(interaction: ChatInputCommandInteraction) :
        PaginatedMessage(scope, interaction) {

        override suspend fun getItemCount(): Int =
            inventoryService.getGoldLeaderboard(PageRequest.of(0, 1)).totalElements.toInt()

        override suspend fun MessageBuilder.renderPage(page: PageRequest) {
            val entries = inventoryService.getGoldLeaderboard(page)
            val offset = page.pageNumber * page.pageSize

            embed {
                title = ":money_bag: Gold Leaderboard"

                description = when {
                    entries.isEmpty -> "No entries yet."
                    else -> entries.mapIndexed { i, entry ->
                        val rank = offset + i + 1
                        "**#$rank** ${entry.amount} gold · <@${entry.userId.value}>"
                    }.joinToString("\n")
                }

                pageFooter()
            }
        }
    }

    private inner class MintLeaderboardMessage(interaction: ChatInputCommandInteraction) :
        PaginatedMessage(scope, interaction) {

        override suspend fun getItemCount(): Int =
            collectionValueService.getMintLeaderboard(PageRequest.of(0, 1)).totalElements.toInt()

        override suspend fun MessageBuilder.renderPage(page: PageRequest) {
            val entries = collectionValueService.getMintLeaderboard(page)
            val offset = page.pageNumber * page.pageSize

            embed {
                title = "✨ Most Mint Cards"

                description = when {
                    entries.isEmpty -> "No entries yet."
                    else -> entries.mapIndexed { i, entry ->
                        val rank = offset + i + 1
                        "**#$rank** ${entry.mintCount} mint cards · <@${entry.userId.value}>"
                    }.joinToString("\n")
                }

                pageFooter()
            }
        }
    }

    private inner class BurnsLeaderboardMessage(interaction: ChatInputCommandInteraction) :
        PaginatedMessage(scope, interaction) {

        override suspend fun getItemCount(): Int =
            userStatsService.getLeaderboard(UserAction.BURN, PageRequest.of(0, 1)).totalElements.toInt()

        override suspend fun MessageBuilder.renderPage(page: PageRequest) {
            val entries = userStatsService.getLeaderboard(UserAction.BURN, page)
            val offset = page.pageNumber * page.pageSize

            embed {
                title = "Most Cards Burned"

                description = when {
                    entries.isEmpty -> "No entries yet."
                    else -> entries.mapIndexed { i, entry ->
                        val rank = offset + i + 1
                        "**#$rank** ${entry.count} cards burned · <@${entry.userId.value}>"
                    }.joinToString("\n")
                }

                pageFooter()
            }
        }
    }

    private inner class DropsLeaderboardMessage(interaction: ChatInputCommandInteraction) :
        PaginatedMessage(scope, interaction) {

        override suspend fun getItemCount(): Int =
            userStatsService.getLeaderboard(UserAction.DROP, PageRequest.of(0, 1)).totalElements.toInt()

        override suspend fun MessageBuilder.renderPage(page: PageRequest) {
            val entries = userStatsService.getLeaderboard(UserAction.DROP, page)
            val offset = page.pageNumber * page.pageSize

            embed {
                title = "Most Drops Created"

                description = when {
                    entries.isEmpty -> "No entries yet."
                    else -> entries.mapIndexed { i, entry ->
                        val rank = offset + i + 1
                        "**#$rank** ${entry.count} drops · <@${entry.userId.value}>"
                    }.joinToString("\n")
                }

                pageFooter()
            }
        }
    }

    private inner class ClaimsLeaderboardMessage(interaction: ChatInputCommandInteraction) :
        PaginatedMessage(scope, interaction) {

        override suspend fun getItemCount(): Int =
            userStatsService.getLeaderboard(UserAction.CLAIM, PageRequest.of(0, 1)).totalElements.toInt()

        override suspend fun MessageBuilder.renderPage(page: PageRequest) {
            val entries = userStatsService.getLeaderboard(UserAction.CLAIM, page)
            val offset = page.pageNumber * page.pageSize

            embed {
                title = "Most Cards Claimed"

                description = when {
                    entries.isEmpty -> "No entries yet."
                    else -> entries.mapIndexed { i, entry ->
                        val rank = offset + i + 1
                        "**#$rank** ${entry.count} cards claimed · <@${entry.userId.value}>"
                    }.joinToString("\n")
                }

                pageFooter()
            }
        }
    }
}
