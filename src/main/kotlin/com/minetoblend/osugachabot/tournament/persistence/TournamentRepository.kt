package com.minetoblend.osugachabot.tournament.persistence

import com.minetoblend.osugachabot.tournament.TournamentStatus
import org.springframework.data.jpa.repository.JpaRepository

interface TournamentRepository : JpaRepository<TournamentEntity, Long> {
    fun findFirstByStatusOrderByCreatedAtDesc(status: TournamentStatus): TournamentEntity?
}
