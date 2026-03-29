package com.minetoblend.osugachabot.trading

import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.users.UserId

interface TradeService {
    fun createTrade(
        initiatorUserId: UserId,
        targetUserId: UserId,
        offeredCardId: CardReplicaId,
        requestedCardId: CardReplicaId,
    ): CreateTradeResult

    fun acceptTrade(tradeId: TradeId, userId: UserId): AcceptTradeResult

    fun declineTrade(tradeId: TradeId, userId: UserId): DeclineTradeResult

    fun cancelTrade(tradeId: TradeId, userId: UserId): CancelTradeResult
}

sealed interface CreateTradeResult {
    data class Created(
        val trade: Trade,
        val offeredCard: CardReplica,
        val requestedCard: CardReplica,
    ) : CreateTradeResult
    data object OfferedCardNotFound : CreateTradeResult
    data object OfferedCardNotOwned : CreateTradeResult
    data object RequestedCardNotFound : CreateTradeResult
    data object RequestedCardNotOwned : CreateTradeResult
    data object CannotTradeWithSelf : CreateTradeResult
}

sealed interface AcceptTradeResult {
    data class Accepted(
        val trade: Trade,
        val offeredCard: CardReplica,
        val requestedCard: CardReplica,
    ) : AcceptTradeResult
    data object TradeNotFound : AcceptTradeResult
    data object NotTargetUser : AcceptTradeResult
    data object TradeNoLongerValid : AcceptTradeResult
    data object CardNoLongerAvailable : AcceptTradeResult
}

sealed interface DeclineTradeResult {
    data class Declined(val trade: Trade) : DeclineTradeResult
    data object TradeNotFound : DeclineTradeResult
    data object NotTargetUser : DeclineTradeResult
    data object TradeNoLongerValid : DeclineTradeResult
}

sealed interface CancelTradeResult {
    data class Cancelled(val trade: Trade) : CancelTradeResult
    data object TradeNotFound : CancelTradeResult
    data object NotInitiator : CancelTradeResult
    data object TradeNoLongerValid : CancelTradeResult
}
