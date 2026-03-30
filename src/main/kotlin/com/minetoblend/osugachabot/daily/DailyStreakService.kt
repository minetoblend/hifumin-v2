package com.minetoblend.osugachabot.daily

import com.minetoblend.osugachabot.users.UserId

interface DailyStreakService {
    /**
     * Records a daily claim for the user, incrementing the streak if claimed within 48 hours
     * of the previous claim, or resetting to 1 if more than 48 hours have passed.
     */
    fun recordClaim(userId: UserId): DailyStreak

    /**
     * Returns the current streak for the user, or a streak of 0 if the user has never claimed.
     */
    fun getStreak(userId: UserId): DailyStreak
}
