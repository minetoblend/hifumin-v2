package com.minetoblend.osugachabot.drops

import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.users.UserId

interface DropService {
    fun createDrop(): Drop
    fun claimCard(dropId: DropId, cardIndex: Int, userId: UserId): ClaimResult
}

sealed class ClaimResult {
    data class Claimed(val drop: Drop, val replica: CardReplica) : ClaimResult()
    data class AlreadyClaimed(val drop: Drop) : ClaimResult()
    data object DropNotFound : ClaimResult()
}
