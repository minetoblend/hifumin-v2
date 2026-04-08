package com.minetoblend.osugachabot.stats

import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface UserStatsService {
    fun getStats(userId: UserId): Map<UserAction, Long>
    fun getLeaderboard(action: UserAction, pageable: Pageable): Page<UserActionEntry>
}
