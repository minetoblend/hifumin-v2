package com.minetoblend.osugachabot.cards.persistence

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "cards",
    indexes = [
        Index("idx_username", "username"),
        Index("idx_country_code", "country_code"),
        Index("idx_global_rank", "global_rank"),
    ]
)
class CardEntity(
    var userId: Long,
    var username: String,
    var countryCode: String,
    var title: String?,
    var followerCount: Int,
    var globalRank: Int?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}