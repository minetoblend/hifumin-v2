package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.users.UserId

interface CardReplicaService {
    fun findById(id: CardReplicaId): CardReplica?
    fun findLatestByUserId(userId: UserId): CardReplica?
    fun findOwnedCardOrLatest(id: CardReplicaId?, userId: UserId): OwnedCardResult

    fun burnCard(id: CardReplicaId, userId: UserId): BurnCardResult
}

sealed interface OwnedCardResult {
    data class Success(val replica: CardReplica) : OwnedCardResult
    data object NotFound : OwnedCardResult
    data object NotOwned : OwnedCardResult
}

sealed interface BurnCardResult {
    data object Success : BurnCardResult
    data object NotFound : BurnCardResult
    data object NotOwned : BurnCardResult
}