package com.minetoblend.osugachabot.tournament

import java.time.Instant

@JvmInline
value class TournamentId(val value: Long)

data class Tournament(
    val id: TournamentId,
    val name: String,
    val status: TournamentStatus,
    val createdAt: Instant,
    val resolvedAt: Instant?,
    val entries: List<TournamentEntry> = emptyList(),
    val placements: List<TournamentPlacement> = emptyList(),
)

enum class TournamentStatus {
    OPEN,
    RESOLVED,
}
