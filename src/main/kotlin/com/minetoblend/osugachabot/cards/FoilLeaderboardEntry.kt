package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.users.UserId

data class FoilLeaderboardEntry(
    val userId: UserId,
    val foilCount: Long,
)
