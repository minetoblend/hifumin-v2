package com.minetoblend.osugachabot.stats

import com.minetoblend.osugachabot.users.UserId

interface UserStatsService {
    fun getStats(userId: UserId): Map<UserAction, Long>
}
