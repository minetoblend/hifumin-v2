package com.minetoblend.osugachabot.drops.application

import com.minetoblend.osugachabot.cards.*
import com.minetoblend.osugachabot.cards.application.computeBurnValue
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.cooldown.CooldownResult
import com.minetoblend.osugachabot.cooldown.CooldownService
import com.minetoblend.osugachabot.cooldown.CooldownType
import com.minetoblend.osugachabot.drops.*
import com.minetoblend.osugachabot.drops.persistence.DropEntity
import com.minetoblend.osugachabot.drops.persistence.DropRepository
import com.minetoblend.osugachabot.drops.persistence.DroppedCardEntity
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.inventory.RemoveItemsResult
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinInstant

@Service
class DropServiceImpl(
    private val cardService: CardService,
    private val cardRepository: CardRepository,
    private val dropRepository: DropRepository,
    private val cardReplicaRepository: CardReplicaRepository,
    private val cooldownService: CooldownService,
    private val inventoryService: InventoryService,
    private val eventPublisher: ApplicationEventPublisher,
) : DropService {
    override fun dropCooldownDuration(): Duration = cooldownService.durationFor(DROP)

    override fun claimCooldownDuration(): Duration = cooldownService.durationFor(CLAIM)

    override fun dropExpiryDuration(): Duration = 60.seconds

    @Transactional
    override fun createDrop(userId: UserId): CreateDropResult {
        when (val cooldown = cooldownService.tryConsume(userId, DROP)) {
            is OnCooldown -> return CreateDropResult.OnCooldown(cooldown.remaining)
            Ready -> {}
        }

        val cards = cardService.getRandomCards(DROP_SIZE)
        val entity = dropRepository.save(DropEntity().also { it.createdByUserId = userId.value })

        cards.forEachIndexed { index, card ->
            entity.cards.add(
                DroppedCardEntity(
                    drop = entity,
                    cardIndex = index,
                    card = cardRepository.getReferenceById(card.id.value),
                    condition = rollRandomCondition(),
                )
            )
        }

        val drop = dropRepository.save(entity).toDomain()
        eventPublisher.publishEvent(DropCreatedEvent(userId))
        return CreateDropResult.Created(drop)
    }

    @Transactional
    override fun createSuperDrop(userId: UserId): Drop {
        val cards = cardService.getRandomCards(SUPER_DROP_SIZE).toMutableList()

        if (cards.none { it.rarity >= SR }) {
            val (rareCard) = cardService.getRandomCardsWithMinimumRarity(1, SR)

            cards[cards.indices.random()] = rareCard
        }

        val entity = dropRepository.save(DropEntity().also { it.createdByUserId = userId.value })
        cards.forEachIndexed { index, card ->
            entity.cards.add(
                DroppedCardEntity(
                    drop = entity,
                    cardIndex = index,
                    card = cardRepository.getReferenceById(card.id.value),
                    condition = rollRandomCondition(),
                )
            )
        }

        return dropRepository.save(entity).toDomain()
    }

    @Transactional
    override fun claimCard(dropId: DropId, cardIndex: Int, userId: UserId): ClaimResult {
        val drop = dropRepository.findByIdOrNull(dropId.value) ?: return ClaimResult.DropNotFound

        if (Clock.System.now() > drop.createdAt.toKotlinInstant() + dropExpiryDuration())
            return ClaimResult.Expired

        val droppedCard = drop.cards.find { it.cardIndex == cardIndex } ?: return ClaimResult.DropNotFound

        if (droppedCard.claimedByUserId != null)
            return ClaimResult.AlreadyClaimed(drop.toDomain())

        when (val cooldown = cooldownService.tryConsume(userId, CooldownType.CLAIM)) {
            is CooldownResult.OnCooldown -> when (inventoryService.removeItems(userId, ItemType.FreeClaim, 1)) {
                RemoveItemsResult.InsufficientItems -> return ClaimResult.OnCooldown(cooldown.remaining)
                RemoveItemsResult.Success -> {}
            }
            CooldownResult.Ready -> {}
        }

        droppedCard.claimedByUserId = userId.value

        val replicaEntity = CardReplicaEntity(
            card = droppedCard.card,
            userId = userId.value,
            condition = droppedCard.condition,
            burnValue = computeBurnValue(droppedCard.card.followerCount, droppedCard.condition),
        )

        val replica = cardReplicaRepository.save(replicaEntity)

        return ClaimResult.Claimed(drop.toDomain(), replica.toDomain())
    }

    private fun rollRandomCondition(): CardCondition {
        val distribution = listOf<CardCondition>(
            Mint,
            Good, Good,
            Poor, Poor, Poor,
            Damaged, Damaged,
        )

        return distribution.random()
    }

    private fun DropEntity.toDomain() = Drop(
        id = DropId(id),
        cards = cards.map { it.toDomain() },
        createdAt = createdAt,
    )

    private fun DroppedCardEntity.toDomain() = DroppedCard(
        index = cardIndex,
        card = card.toDomain(),
        condition = condition,
        claimedBy = claimedByUserId?.let { UserId(it) },
    )

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

    companion object {
        private const val DROP_SIZE = 3
        private const val SUPER_DROP_SIZE = 10
    }
}
