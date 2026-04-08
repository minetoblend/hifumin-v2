package com.minetoblend.osugachabot.cards.application

import com.minetoblend.osugachabot.cards.*
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.inventory.RemoveItemsResult
import com.minetoblend.osugachabot.cards.CardBurnedEvent
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class CardReplicaServiceImpl(
    private val cardReplicaRepository: CardReplicaRepository,
    private val inventoryService: InventoryService,
    private val eventPublisher: ApplicationEventPublisher,
    private val random: Random,
) : CardReplicaService {
    override fun findById(id: CardReplicaId): CardReplica? =
        cardReplicaRepository.findByIdOrNull(id.value)?.toDomain()

    override fun findLatestByUserId(userId: UserId): CardReplica? =
        cardReplicaRepository.findFirstByUserIdOrderByCreatedAtDesc(userId.value)?.toDomain()

    override fun getCardCount(userId: UserId): Int =
        cardReplicaRepository.countByUserId(userId.value)

    override fun findByUserId(userId: UserId, pageable: Pageable): Page<CardReplica> {
        return cardReplicaRepository.findByUserId(userId.value, pageable).map { it.toDomain() }
    }

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

    @Transactional
    override fun burnCard(id: CardReplicaId, userId: UserId): BurnCardResult {
        val card = cardReplicaRepository.findByIdOrNull(id.value) ?: return BurnCardResult.NotFound

        if (userId != card.userId.toUserId())
            return BurnCardResult.NotOwned

        val replica = card.toDomain()
        cardReplicaRepository.delete(card)
        inventoryService.addItems(userId, ItemType.Gold, replica.burnValue.toLong())
        eventPublisher.publishEvent(CardBurnedEvent(userId))

        return BurnCardResult.Success
    }

    @Transactional
    override fun upgradeCard(id: CardReplicaId, userId: UserId): UpgradeCardResult {
        val entity = cardReplicaRepository.findByIdOrNull(id.value)
            ?: return UpgradeCardResult.NotFound

        if (userId != entity.userId.toUserId())
            return UpgradeCardResult.NotOwned

        if (entity.condition == CardCondition.Mint)
            return UpgradeCardResult.AlreadyMint

        val cost = entity.condition.upgradeCost
        when (inventoryService.removeItems(userId, ItemType.Gold, cost)) {
            RemoveItemsResult.InsufficientItems -> return UpgradeCardResult.InsufficientGold
            RemoveItemsResult.Success -> {}
        }

        return if (random.nextDouble() < entity.condition.upgradeSuccessRate) {
            entity.condition = entity.condition.nextCondition()
            UpgradeCardResult.Success(entity.condition)
        } else {
            UpgradeCardResult.Failed(entity.condition)
        }
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
        foil = foil,
    )
}