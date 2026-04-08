package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.users.UserId

data class MintLeaderboardEntry(
    val userId: UserId,
    val mintCount: Long,
)
