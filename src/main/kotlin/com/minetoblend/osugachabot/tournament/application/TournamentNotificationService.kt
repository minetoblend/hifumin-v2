package com.minetoblend.osugachabot.tournament.application

import com.minetoblend.osugachabot.discord.DiscordMessagingService
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.tournament.TournamentEntry
import com.minetoblend.osugachabot.tournament.TournamentPlacement
import com.minetoblend.osugachabot.tournament.TournamentResolution
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TournamentNotificationService(
    private val messagingService: DiscordMessagingService,
    private val cardRenderer: CardRenderer,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun notifyParticipants(resolution: TournamentResolution) {
        if (resolution.placements.isEmpty()) return

        val entries = resolution.tournament.entries
        val placementsByUserId = resolution.placements.associateBy { it.userId }
        val tournamentName = resolution.tournament.name
        val winner = resolution.placements.first()

        val bracket = resolution.tournament.bracket
        val bracketImage = if (bracket != null && bracket.rounds.isNotEmpty()) {
            runCatching { cardRenderer.renderBracket(bracket, tournamentName) }
                .onFailure { e -> logger.error("Failed to render bracket image: ${e.message}", e) }
                .getOrNull()
        } else null

        val (guildEntries, dmEntries) = entries.partition { it.guildId != null }

        // One message per guild: conclusion header + placement listing (+ bracket image if available)
        guildEntries.groupBy { it.guildId }.forEach { (_, guildGroupEntries) ->
            val channelId = guildGroupEntries.first().channelId
            val placementListing = guildGroupEntries.joinToString("\n") { entry ->
                val placement = placementsByUserId[entry.userId]
                val line = if (placement != null) {
                    buildPlacementMessage(placement, tournamentName)
                } else {
                    buildNonPlacedMessage(winner, tournamentName)
                }
                "<@${entry.userId.value}> $line"
            }
            val message = "The **$tournamentName** has concluded! <@${winner.userId.value}> wins!\n$placementListing"
            try {
                if (bracketImage != null) {
                    messagingService.sendChannelMessageWithImage(channelId, message, bracketImage, "bracket.png")
                } else {
                    messagingService.sendChannelMessage(channelId, message)
                }
            } catch (e: Exception) {
                logger.error("Failed to send tournament notification to guild channel $channelId: ${e.message}", e)
            }
        }

        for (entry in dmEntries) {
            val placement = placementsByUserId[entry.userId]
            val message = if (placement != null) {
                buildPlacementMessage(placement, tournamentName)
            } else {
                buildNonPlacedMessage(winner, tournamentName)
            }
            try {
                messagingService.sendDm(entry.userId, message)
            } catch (e: Exception) {
                logger.error("Failed to send DM for tournament entry ${entry.id}: ${e.message}", e)
            }
        }
    }

    private fun buildPlacementMessage(placement: TournamentPlacement, tournamentName: String): String {
        val placeStr = when (placement.place) {
            1 -> "1st"
            2 -> "2nd"
            3 -> "3rd"
            else -> "${placement.place}th"
        }
        val prizeDetails = buildString {
            append("**${placement.prizeGold} gold**")
            val replicaId = placement.prizeCardReplicaId?.toDisplayId()
            if (replicaId != null) {
                append(" + a new SSR+ card (`$replicaId`)")
            }
        }
        return "You placed **$placeStr** in the **$tournamentName**! Prize: $prizeDetails."
    }

    private fun buildNonPlacedMessage(winner: TournamentPlacement, tournamentName: String): String {
        return "The **$tournamentName** has ended! <@${winner.userId.value}> won. Better luck next time!"
    }
}
