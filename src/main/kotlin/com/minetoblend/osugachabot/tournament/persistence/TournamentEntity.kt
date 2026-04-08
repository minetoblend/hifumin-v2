package com.minetoblend.osugachabot.tournament.persistence

import com.minetoblend.osugachabot.tournament.TournamentStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "tournaments")
class TournamentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(nullable = false)
    var name: String = ""

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TournamentStatus = TournamentStatus.OPEN

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @Column(nullable = true)
    var resolvedAt: Instant? = null

    @OneToMany(mappedBy = "tournament", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var entries: MutableList<TournamentEntryEntity> = mutableListOf()

    @OneToMany(mappedBy = "tournament", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @OrderBy("place ASC")
    var placements: MutableList<TournamentPlacementEntity> = mutableListOf()
}
