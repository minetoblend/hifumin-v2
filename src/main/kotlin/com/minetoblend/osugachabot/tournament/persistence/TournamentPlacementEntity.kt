package com.minetoblend.osugachabot.tournament.persistence

import jakarta.persistence.*

@Entity
@Table(
    name = "tournament_placements",
    indexes = [
        Index(name = "idx_tournament_placement_unique", columnList = "tournament_id, place", unique = true),
    ]
)
class TournamentPlacementEntity(
    @ManyToOne(optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: TournamentEntity,

    @Column(nullable = false)
    var place: Int,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "prize_gold", nullable = false)
    var prizeGold: Long,

    @Column(name = "prize_card_replica_id", nullable = true)
    var prizeCardReplicaId: Long? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}
