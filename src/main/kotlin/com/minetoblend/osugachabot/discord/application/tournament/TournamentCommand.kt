package com.minetoblend.osugachabot.discord.application.tournament

import com.minetoblend.osugachabot.cards.CardReplicaId.Companion.toCardReplicaIdOrNull
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.utils.cardId
import com.minetoblend.osugachabot.discord.utils.toDiscordRelativeTimestamp
import com.minetoblend.osugachabot.tournament.EnterTournamentResult
import com.minetoblend.osugachabot.tournament.TournamentService
import com.minetoblend.osugachabot.tournament.application.TournamentScheduler
import com.minetoblend.osugachabot.tournament.application.TournamentServiceImpl
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.builder.message.embed
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.milliseconds

@Component
class TournamentCommand(
    private val tournamentService: TournamentService,
) : SlashCommand {
    override val name = "tournament"
    override val description = "Enter or view the current tournament"

    override fun ChatInputCreateBuilder.declare() {
        subCommand("enter", "Enter the current tournament with one of your cards") {
            cardId("card", required = true, description = "The card ID to enter with (e.g. aaaa)")
        }
        subCommand("info", "View information about the current tournament") {}
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        when (val cmd = interaction.command) {
            is SubCommand -> when (cmd.name) {
                "enter" -> handleEnter()
                "info" -> handleInfo()
            }

            else -> {}
        }
    }

    private suspend fun ChatInputCommandInteractionCreateEvent.handleEnter() {
        val cardIdStr = interaction.command.strings["card"] ?: run {
            interaction.respondEphemeral { content = "Please provide a card ID." }
            return
        }

        val cardReplicaId = cardIdStr.toCardReplicaIdOrNull() ?: run {
            interaction.respondEphemeral { content = "Invalid card ID: `$cardIdStr`" }
            return
        }

        val userId = interaction.user.id.toUserId()
        val channelId = interaction.channelId.value.toLong()
        val guildId = (interaction as? GuildChatInputCommandInteraction)
            ?.guildId?.value?.toLong()

        when (val result = tournamentService.enterTournament(userId, cardReplicaId, channelId, guildId)) {
            is EnterTournamentResult.Entered -> {
                interaction.respondPublic {
                    content =
                        "${interaction.user.mention} entered the **${result.tournament.name}** with card `${cardReplicaId.toDisplayId()}`!"
                }
            }

            EnterTournamentResult.NoActiveTournament -> {
                interaction.respondEphemeral { content = "There is no active tournament right now." }
            }

            EnterTournamentResult.AlreadyEntered -> {
                interaction.respondEphemeral { content = "You have already entered this tournament!" }
            }

            EnterTournamentResult.CardNotOwned -> {
                interaction.respondEphemeral { content = "You don't own that card!" }
            }
        }
    }

    private suspend fun ChatInputCommandInteractionCreateEvent.handleInfo() {
        val tournament = tournamentService.getActiveTournament()

        if (tournament == null) {
            interaction.respondEphemeral { content = "There is no active tournament right now." }
            return
        }

        val endsAt = tournament.createdAt.toEpochMilli() + TournamentScheduler.TOURNAMENT_DURATION.inWholeMilliseconds
        val remaining = (endsAt - System.currentTimeMillis()).milliseconds

        interaction.respondPublic {
            embed {
                title = tournament.name
                description = buildString {
                    appendLine("**Entries:** ${tournament.entries.size}")
                    appendLine("**Prize:** ${TournamentServiceImpl.PRIZE_GOLD} gold + SSR+ card (Mint)")
                    appendLine("**Ends:** ${remaining.toDiscordRelativeTimestamp()}")
                }

                footer {
                    text = """
                           A tournament takes place every 12 hours.
                           Enter with one of your cards using /tournament enter - the stronger your card, the better your odds!
                           The winner is randomly selected, weighted by card stats.
                           """.trimIndent()
                }
            }
        }
    }
}
