package com.minetoblend.osugachabot.trading.application

import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.trading.*
import com.minetoblend.osugachabot.trading.persistence.TradeEntity
import com.minetoblend.osugachabot.trading.persistence.TradeRepository
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.trading.TradeAcceptedEvent
import com.minetoblend.osugachabot.trading.TradeInitiatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TradeServiceImpl(
    private val tradeRepository: TradeRepository,
    private val cardReplicaRepository: CardReplicaRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : TradeService {

    @Transactional
    override fun createTrade(
        initiatorUserId: UserId,
        targetUserId: UserId,
        offeredCardId: CardReplicaId,
        requestedCardId: CardReplicaId,
    ): CreateTradeResult {
        if (initiatorUserId == targetUserId) return CannotTradeWithSelf

        val offeredCard = cardReplicaRepository.findByIdOrNull(offeredCardId.value)
            ?: return OfferedCardNotFound

        if (offeredCard.userId != initiatorUserId.value)
            return OfferedCardNotOwned

        val requestedCard = cardReplicaRepository.findByIdOrNull(requestedCardId.value)
            ?: return RequestedCardNotFound

        if (requestedCard.userId != targetUserId.value)
            return RequestedCardNotOwned

        val entity = tradeRepository.save(
            TradeEntity(
                initiatorUserId = initiatorUserId.value,
                targetUserId = targetUserId.value,
                offeredCardId = offeredCardId.value,
                requestedCardId = requestedCardId.value,
            )
        )

        eventPublisher.publishEvent(TradeInitiatedEvent(initiatorUserId))

        return CreateTradeResult.Created(
            trade = entity.toDomain(),
            offeredCard = offeredCard.toDomain(),
            requestedCard = requestedCard.toDomain(),
        )
    }

    @Transactional
    override fun acceptTrade(tradeId: TradeId, userId: UserId): AcceptTradeResult {
        val trade = tradeRepository.findByIdOrNull(tradeId.value) ?: return TradeNotFound

        if (trade.targetUserId != userId.value) return NotTargetUser

        if (trade.status != Pending) return TradeNoLongerValid

        val offeredCard = cardReplicaRepository.findByIdOrNull(trade.offeredCardId)
        val requestedCard = cardReplicaRepository.findByIdOrNull(trade.requestedCardId)

        if (offeredCard == null || requestedCard == null ||
            offeredCard.userId != trade.initiatorUserId ||
            requestedCard.userId != trade.targetUserId
        ) {
            trade.status = Cancelled
            tradeRepository.save(trade)
            return CardNoLongerAvailable
        }

        // Swap ownership
        offeredCard.userId = trade.targetUserId
        requestedCard.userId = trade.initiatorUserId
        cardReplicaRepository.save(offeredCard)
        cardReplicaRepository.save(requestedCard)

        trade.status = Accepted
        tradeRepository.save(trade)

        eventPublisher.publishEvent(TradeAcceptedEvent(userId))

        return AcceptTradeResult.Accepted(
            trade = trade.toDomain(),
            offeredCard = offeredCard.toDomain(),
            requestedCard = requestedCard.toDomain(),
        )
    }

    @Transactional
    override fun declineTrade(tradeId: TradeId, userId: UserId): DeclineTradeResult {
        val trade = tradeRepository.findByIdOrNull(tradeId.value)
            ?: return TradeNotFound

        if (trade.targetUserId != userId.value)
            return NotTargetUser

        if (trade.status != TradeStatus.Pending)
            return TradeNoLongerValid

        trade.status = TradeStatus.Declined
        tradeRepository.save(trade)

        return DeclineTradeResult.Declined(trade.toDomain())
    }

    @Transactional
    override fun cancelTrade(tradeId: TradeId, userId: UserId): CancelTradeResult {
        val trade = tradeRepository.findByIdOrNull(tradeId.value)
            ?: return CancelTradeResult.TradeNotFound

        if (trade.initiatorUserId != userId.value)
            return CancelTradeResult.NotInitiator

        if (trade.status != TradeStatus.Pending)
            return CancelTradeResult.TradeNoLongerValid

        trade.status = TradeStatus.Cancelled
        tradeRepository.save(trade)

        return CancelTradeResult.Cancelled(trade.toDomain())
    }

    private fun TradeEntity.toDomain() = Trade(
        id = TradeId(id),
        initiatorUserId = initiatorUserId.toUserId(),
        targetUserId = targetUserId.toUserId(),
        offeredCardId = CardReplicaId(offeredCardId),
        requestedCardId = CardReplicaId(requestedCardId),
        status = status,
        createdAt = createdAt,
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
        foil = foil,
    )
}
