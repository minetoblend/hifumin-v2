package com.minetoblend.osugachabot.leaderboard

import com.minetoblend.osugachabot.cards.FoilLeaderboardEntry
import com.minetoblend.osugachabot.cards.MintLeaderboardEntry
import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LeaderboardService {
    fun getLeaderboard(pageable: Pageable): Page<CollectionValueEntry>
    fun getLargestCollectionLeaderboard(pageable: Pageable): Page<CollectionValueEntry>
    fun getMintLeaderboard(pageable: Pageable): Page<MintLeaderboardEntry>
    fun getFoilLeaderboard(pageable: Pageable): Page<FoilLeaderboardEntry>
    fun getCollectionValue(userId: UserId): CollectionValueEntry
    fun recompute(userId: UserId)
    fun recomputeAll()
}
