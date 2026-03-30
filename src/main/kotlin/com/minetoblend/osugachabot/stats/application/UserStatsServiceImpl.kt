package com.minetoblend.osugachabot.stats.application

import com.minetoblend.osugachabot.cards.CardBurnedEvent
import com.minetoblend.osugachabot.daily.DailyClaimedEvent
import com.minetoblend.osugachabot.drops.CardClaimedEvent
import com.minetoblend.osugachabot.drops.DropCreatedEvent
import com.minetoblend.osugachabot.drops.SuperDropCreatedEvent
import com.minetoblend.osugachabot.stats.UserAction
import com.minetoblend.osugachabot.stats.UserStatsService
import com.minetoblend.osugachabot.stats.persistence.UserActionStatEntity
import com.minetoblend.osugachabot.stats.persistence.UserActionStatId
import com.minetoblend.osugachabot.stats.persistence.UserActionStatRepository
import com.minetoblend.osugachabot.trading.TradeAcceptedEvent
import com.minetoblend.osugachabot.trading.TradeInitiatedEvent
import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener

@Service
class UserStatsServiceImpl(
    private val repository: UserActionStatRepository,
) : UserStatsService {

    override fun getStats(userId: UserId): Map<UserAction, Long> {
        val entities = repository.findByIdUserId(userId.value)
        val byAction = entities.associate { it.id.action to it.count }
        return UserAction.entries.associateWith { action -> byAction[action.name] ?: 0L }
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onDropCreated(event: DropCreatedEvent) = increment(event.userId, DROP)

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onCardClaimed(event: CardClaimedEvent) = increment(event.userId, CLAIM)

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onCardBurned(event: CardBurnedEvent) = increment(event.userId, BURN)

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onDailyClaimed(event: DailyClaimedEvent) = increment(event.userId, DAILY)

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onTradeInitiated(event: TradeInitiatedEvent) = increment(event.userId, TRADE_INITIATED)

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onTradeAccepted(event: TradeAcceptedEvent) = increment(event.userId, TRADE_ACCEPTED)

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onSuperDropCreated(event: SuperDropCreatedEvent) = increment(event.userId, SUPER_DROP)

    private fun increment(userId: UserId, action: UserAction) {
        val id = UserActionStatId(userId.value, action.name)
        val entity = repository.findByIdOrNull(id) ?: UserActionStatEntity(id, 0L)
        entity.count++
        repository.save(entity)
    }
}
