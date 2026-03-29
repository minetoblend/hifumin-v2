package com.minetoblend.osugachabot.trading

import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.users.UserId
import java.time.Instant

@JvmInline
value class TradeId(val value: Long)

enum class TradeStatus {
    Pending,
    Accepted,
    Declined,
    Cancelled,
}

data class Trade(
    val id: TradeId,
    val initiatorUserId: UserId,
    val targetUserId: UserId,
    val offeredCardId: CardReplicaId,
    val requestedCardId: CardReplicaId,
    val status: TradeStatus,
    val createdAt: Instant,
)
