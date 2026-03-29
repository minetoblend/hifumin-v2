package com.minetoblend.osugachabot.trading.persistence

import com.minetoblend.osugachabot.trading.TradeStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "trades",
    indexes = [
        Index(name = "idx_trade_initiator", columnList = "initiator_user_id"),
        Index(name = "idx_trade_target", columnList = "target_user_id"),
    ]
)
class TradeEntity(
    @Column(name = "initiator_user_id", nullable = false)
    var initiatorUserId: Long,

    @Column(name = "target_user_id", nullable = false)
    var targetUserId: Long,

    @Column(name = "offered_card_id", nullable = false)
    var offeredCardId: Long,

    @Column(name = "requested_card_id", nullable = false)
    var requestedCardId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TradeStatus = TradeStatus.Pending,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}
