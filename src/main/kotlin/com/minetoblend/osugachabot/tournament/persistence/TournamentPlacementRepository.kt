package com.minetoblend.osugachabot.tournament.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TournamentPlacementRepository : JpaRepository<TournamentPlacementEntity, Long> {
    fun findByTournamentIdOrderByPlace(tournamentId: Long): List<TournamentPlacementEntity>
}
