package com.minetoblend.osugachabot.leaderboard

import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CollectionValueService {
    fun getLeaderboard(pageable: Pageable): Page<CollectionValueEntry>
    fun getCollectionValue(userId: UserId): CollectionValueEntry
    fun recompute(userId: UserId)
    fun recomputeAll()
}
