package com.minetoblend.osugachabot.tournament.application

import com.minetoblend.osugachabot.discord.DiscordMessagingService
import com.minetoblend.osugachabot.tournament.TournamentEntry
import com.minetoblend.osugachabot.tournament.TournamentPlacement
import com.minetoblend.osugachabot.tournament.TournamentResolution
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TournamentNotificationService(
    private val messagingService: DiscordMessagingService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun notifyParticipants(resolution: TournamentResolution) {
        if (resolution.placements.isEmpty()) return

        val entries = resolution.tournament.entries
        val placementsByUserId = resolution.placements.associateBy { it.userId }
        val tournamentName = resolution.tournament.name

        for (entry in entries) {
            val placement = placementsByUserId[entry.userId]
            val message = if (placement != null) {
                buildPlacementMessage(placement, tournamentName)
            } else {
                val winner = resolution.placements.first()
                buildNonPlacedMessage(winner, tournamentName)
            }

            sendNotification(entry, message)
        }
    }

    private suspend fun sendNotification(entry: TournamentEntry, message: String) {
        try {
            if (entry.guildId != null) {
                val mention = "<@${entry.userId.value}>"
                messagingService.sendChannelMessage(entry.channelId, "$mention $message")
            } else {
                messagingService.sendDm(entry.userId, message)
            }
        } catch (e: Exception) {
            logger.error("Failed to send notification for tournament entry ${entry.id}: ${e.message}", e)
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
