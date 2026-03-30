package com.minetoblend.osugachabot.leaderboard

import com.minetoblend.osugachabot.users.UserId

data class CollectionValueEntry(
    val userId: UserId,
    val totalValue: Long,
    val cardCount: Int,
)
