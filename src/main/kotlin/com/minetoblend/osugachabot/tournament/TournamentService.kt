package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.users.UserId

interface TournamentService {
    fun getActiveTournament(): Tournament?

    fun enterTournament(
        userId: UserId,
        cardReplicaId: CardReplicaId,
        channelId: Long,
        guildId: Long?,
    ): EnterTournamentResult

    fun resolveTournament(tournamentId: TournamentId): TournamentResolution?

    fun ensureActiveTournament(): Tournament

    fun buildPreviewBracket(tournament: Tournament, viewerUserId: UserId): TournamentBracket
}

sealed interface EnterTournamentResult {
    data class Entered(val tournament: Tournament, val entry: TournamentEntry) : EnterTournamentResult
    data object NoActiveTournament : EnterTournamentResult
    data object AlreadyEntered : EnterTournamentResult
    data object CardNotOwned : EnterTournamentResult
}

data class TournamentResolution(
    val tournament: Tournament,
    val placements: List<TournamentPlacement>,
)
