package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CardReplicaService {
    fun findById(id: CardReplicaId): CardReplica?
    fun findLatestByUserId(userId: UserId): CardReplica?
    fun findOwnedCardOrLatest(id: CardReplicaId?, userId: UserId): OwnedCardResult

    fun burnCard(id: CardReplicaId, userId: UserId): BurnCardResult

    fun upgradeCard(id: CardReplicaId, userId: UserId): UpgradeCardResult

    fun getCardCount(userId: UserId): Int

    fun findByUserId(userId: UserId, pageable: Pageable): Page<CardReplica>
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
    data class Locked(val reason: String) : BurnCardResult
}

sealed interface UpgradeCardResult {
    data class Success(val newCondition: CardCondition) : UpgradeCardResult
    data class Failed(val condition: CardCondition) : UpgradeCardResult
    data object AlreadyMint : UpgradeCardResult
    data object NotFound : UpgradeCardResult
    data object NotOwned : UpgradeCardResult
    data object InsufficientGold : UpgradeCardResult
    data class Locked(val reason: String) : UpgradeCardResult
}