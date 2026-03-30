package com.minetoblend.osugachabot.daily

import com.minetoblend.osugachabot.users.UserId

data class DailyStreak(
    val userId: UserId,
    val currentStreak: Int,
)
