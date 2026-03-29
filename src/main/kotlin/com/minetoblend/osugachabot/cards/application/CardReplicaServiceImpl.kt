package com.minetoblend.osugachabot.cards.application

import com.minetoblend.osugachabot.cards.*
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CardReplicaServiceImpl(
    private val cardReplicaRepository: CardReplicaRepository,
) : CardReplicaService {
    override fun findById(id: CardReplicaId): CardReplica? =
        cardReplicaRepository.findByIdOrNull(id.value)?.toDomain()

    override fun findLatestByUserId(userId: UserId): CardReplica? =
        cardReplicaRepository.findFirstByUserIdOrderByCreatedAtDesc(userId.value)?.toDomain()

    override fun findOwnedCardOrLatest(id: CardReplicaId?, userId: UserId): OwnedCardResult {
        val card = when {
            id != null -> findById(id)
            else -> findLatestByUserId(userId)
        }

        card ?: return OwnedCardResult.NotFound

        if (card.userId != userId)
            return OwnedCardResult.NotOwned

        return OwnedCardResult.Success(card)
    }

    override fun burnCard(id: CardReplicaId, userId: UserId): BurnCardResult {
        val card = cardReplicaRepository.findByIdOrNull(id.value) ?: return BurnCardResult.NotFound

        if (userId != card.userId.toUserId())
            return BurnCardResult.NotOwned

        cardReplicaRepository.delete(card)
        // TODO: gain gold

        return BurnCardResult.Success
    }

    private fun CardEntity.toDomain() = Card(
        id = CardId(id),
        userId = userId,
        username = username,
        countryCode = countryCode,
        title = title,
        followerCount = followerCount,
        globalRank = globalRank,
        rarity = rarity,
    )

    private fun CardReplicaEntity.toDomain() = CardReplica(
        id = CardReplicaId(id),
        card = card.toDomain(),
        userId = userId.toUserId(),
        condition = condition,
    )
}