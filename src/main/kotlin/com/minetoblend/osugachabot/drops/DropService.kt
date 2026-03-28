package com.minetoblend.osugachabot.drops

import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.users.UserId
import kotlin.time.Duration

interface DropService {
    fun expiryDuration(): Duration

    fun createDrop(userId: UserId): CreateDropResult
    fun claimCard(dropId: DropId, cardIndex: Int, userId: UserId): ClaimResult
}

sealed class CreateDropResult {
    data class Created(val drop: Drop) : CreateDropResult()
    data class OnCooldown(val remaining: Duration) : CreateDropResult()
}

sealed class ClaimResult {
    data class Claimed(val drop: Drop, val replica: CardReplica) : ClaimResult()
    data class AlreadyClaimed(val drop: Drop) : ClaimResult()
    data object DropNotFound : ClaimResult()
    data object Expired : ClaimResult()
}
