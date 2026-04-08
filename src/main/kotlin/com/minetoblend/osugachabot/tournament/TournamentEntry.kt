package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.users.UserId

data class TournamentEntry(
    val id: Long,
    val tournamentId: TournamentId,
    val userId: UserId,
    val cardReplicaId: CardReplicaId,
    val channelId: Long,
    val guildId: Long?,
    val weight: Double,
)
