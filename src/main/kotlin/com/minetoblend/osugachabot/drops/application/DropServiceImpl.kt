package com.minetoblend.osugachabot.drops.application

import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.cards.CardService
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.drops.ClaimResult
import com.minetoblend.osugachabot.drops.CreateDropResult
import com.minetoblend.osugachabot.drops.Drop
import com.minetoblend.osugachabot.drops.DropId
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.drops.DroppedCard
import com.minetoblend.osugachabot.drops.persistence.DroppedCardEntity
import com.minetoblend.osugachabot.drops.persistence.DropEntity
import com.minetoblend.osugachabot.drops.persistence.DropRepository
import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinInstant

@Service
class DropServiceImpl(
    private val cardService: CardService,
    private val cardRepository: CardRepository,
    private val dropRepository: DropRepository,
    private val cardReplicaRepository: CardReplicaRepository,
) : DropService {
    override fun expiryDuration(): Duration = 60.seconds
    override fun cooldownDuration(): Duration = 10.minutes

    @Transactional
    override fun createDrop(): CreateDropResult {
        val lastDrop = dropRepository.findTopByOrderByCreatedAtDesc()
        if (lastDrop != null) {
            val elapsed = Clock.System.now() - lastDrop.createdAt.toKotlinInstant()
            val remaining = cooldownDuration() - elapsed
            if (remaining.isPositive()) {
                return CreateDropResult.OnCooldown(remaining)
            }
        }

        val cards = cardService.getRandomCards(DROP_SIZE)
        val entity = dropRepository.save(DropEntity())

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

        return CreateDropResult.Created(dropRepository.save(entity).toDomain())
    }

    @Transactional
    override fun claimCard(dropId: DropId, cardIndex: Int, userId: UserId): ClaimResult {
        val drop = dropRepository.findByIdOrNull(dropId.value) ?: return ClaimResult.DropNotFound


        if (Clock.System.now() > drop.createdAt.toKotlinInstant() + expiryDuration())
            return ClaimResult.Expired

        val droppedCard = drop.cards.find { it.cardIndex == cardIndex } ?: return ClaimResult.DropNotFound

        if (droppedCard.claimedByUserId != null)
            return ClaimResult.AlreadyClaimed(drop.toDomain())

        droppedCard.claimedByUserId = userId.value

        val replica = cardReplicaRepository.save(
            CardReplicaEntity(
                card = droppedCard.card,
                userId = userId.value,
                condition = droppedCard.condition,
            )
        )

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
        username = username,
        countryCode = countryCode,
        title = title,
        followerCount = followerCount,
        globalRank = globalRank,
    )

    private fun CardReplicaEntity.toDomain() = CardReplica(
        id = CardReplicaId(id),
        card = card.toDomain(),
        userId = userId,
        condition = condition,
    )

    companion object {
        private const val DROP_SIZE = 3
    }
}
