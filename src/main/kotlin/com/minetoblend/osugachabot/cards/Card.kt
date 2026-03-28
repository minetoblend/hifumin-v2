package com.minetoblend.osugachabot.cards

@JvmInline
value class CardId(val value: Long)

data class Card(
    val id: CardId,
    val userId: Long,
    var username: String,
    var countryCode: String,
    var title: String?,
    var followerCount: Int,
    var globalRank: Int?,
)