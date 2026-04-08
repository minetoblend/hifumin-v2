package com.minetoblend.osugachabot.tournament.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TournamentEntryRepository : JpaRepository<TournamentEntryEntity, Long> {
    fun findByTournamentIdAndUserId(tournamentId: Long, userId: Long): TournamentEntryEntity?

    fun findByTournamentId(tournamentId: Long): List<TournamentEntryEntity>
}
