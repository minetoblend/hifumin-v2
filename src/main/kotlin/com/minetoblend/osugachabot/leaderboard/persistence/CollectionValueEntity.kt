package com.minetoblend.osugachabot.leaderboard.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user_collection_value")
class CollectionValueEntity(
    @Id
    var userId: Long,

    @Column(nullable = false)
    var totalValue: Long,

    @Column(nullable = false)
    var cardCount: Int,
)
