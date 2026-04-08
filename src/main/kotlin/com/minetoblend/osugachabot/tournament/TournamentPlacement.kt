package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.users.UserId

data class TournamentPlacement(
    val id: Long,
    val tournamentId: TournamentId,
    val place: Int,
    val userId: UserId,
    val prizeGold: Long,
    val prizeCardReplicaId: CardReplicaId?,
)
