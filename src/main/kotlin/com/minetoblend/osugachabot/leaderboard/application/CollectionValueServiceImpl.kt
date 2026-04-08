package com.minetoblend.osugachabot.leaderboard.application

import com.minetoblend.osugachabot.cards.CardBurnedEvent
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.MintLeaderboardEntry
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.drops.CardClaimedEvent
import com.minetoblend.osugachabot.leaderboard.CollectionValueEntry
import com.minetoblend.osugachabot.leaderboard.CollectionValueService
import com.minetoblend.osugachabot.leaderboard.persistence.CollectionValueEntity
import com.minetoblend.osugachabot.leaderboard.persistence.CollectionValueRepository
import com.minetoblend.osugachabot.trading.TradeAcceptedEvent
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener

@Service
class CollectionValueServiceImpl(
    private val collectionValueRepository: CollectionValueRepository,
    private val cardReplicaRepository: CardReplicaRepository,
) : CollectionValueService {

    private val log = LoggerFactory.getLogger(CollectionValueServiceImpl::class.java)

    override fun getLeaderboard(pageable: Pageable): Page<CollectionValueEntry> =
        collectionValueRepository.findAllByOrderByTotalValueDesc(pageable).map { it.toDomain() }

    override fun getMintLeaderboard(pageable: Pageable): Page<MintLeaderboardEntry> =
        cardReplicaRepository.countByConditionGroupByUser(CardCondition.Mint, pageable)
            .map { MintLeaderboardEntry(it.getUserId().toUserId(), it.getCount()) }

    override fun getCollectionValue(userId: UserId): CollectionValueEntry =
        collectionValueRepository.findByIdOrNull(userId.value)?.toDomain()
            ?: CollectionValueEntry(userId, 0L, 0)

    @Transactional
    override fun recomputeAll() {
        collectionValueRepository.deleteAll()
        val userIds = cardReplicaRepository.findDistinctUserIds()
        log.info("Recomputing collection value for {} users", userIds.size)
        for (userId in userIds) {
            val totalValue = cardReplicaRepository.sumBurnValueByUserId(userId)
            val cardCount = cardReplicaRepository.countByUserId(userId)
            collectionValueRepository.save(CollectionValueEntity(userId, totalValue, cardCount))
        }
        log.info("Collection value recompute complete")
    }

    @Transactional
    override fun recompute(userId: UserId) {
        val totalValue = cardReplicaRepository.sumBurnValueByUserId(userId.value)
        val cardCount = cardReplicaRepository.countByUserId(userId.value)
        val entity = collectionValueRepository.findByIdOrNull(userId.value)
            ?: CollectionValueEntity(userId.value, 0L, 0)
        entity.totalValue = totalValue
        entity.cardCount = cardCount
        collectionValueRepository.save(entity)
        log.debug("Recomputed collection value for user {}: totalValue={}, cardCount={}", userId.value, totalValue, cardCount)
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onCardClaimed(event: CardClaimedEvent) {
        log.debug("Updating collection value for user {} after card claim", event.userId.value)
        recompute(event.userId)
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onCardBurned(event: CardBurnedEvent) {
        log.debug("Updating collection value for user {} after card burn", event.userId.value)
        recompute(event.userId)
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onTradeAccepted(event: TradeAcceptedEvent) {
        log.debug("Updating collection value for users {} and {} after trade", event.userId.value, event.initiatorUserId.value)
        recompute(event.userId)
        recompute(event.initiatorUserId)
    }

    private fun CollectionValueEntity.toDomain() = CollectionValueEntry(
        userId = userId.toUserId(),
        totalValue = totalValue,
        cardCount = cardCount,
    )
}
