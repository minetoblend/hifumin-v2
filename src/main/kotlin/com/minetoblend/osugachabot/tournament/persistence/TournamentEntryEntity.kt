package com.minetoblend.osugachabot.tournament.persistence

import jakarta.persistence.*

@Entity
@Table(
    name = "tournament_entries",
    indexes = [
        Index(name = "idx_tournament_entry_user", columnList = "tournament_id, user_id", unique = true),
    ]
)
class TournamentEntryEntity(
    @ManyToOne(optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: TournamentEntity,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "card_replica_id", nullable = true)
    var cardReplicaId: Long?,

    @Column(name = "channel_id", nullable = false)
    var channelId: Long,

    @Column(name = "guild_id", nullable = true)
    var guildId: Long? = null,

    @Column(nullable = false)
    var weight: Double = 0.0,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}
