package com.minetoblend.osugachabot.cards.persistence

import com.minetoblend.osugachabot.cards.CardCondition
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "card_replicas",
    indexes = [
        Index("idx_user_id", "user_id"),
        Index("idx_condition", "card_condition"),
        Index("idx_created_at", "created_at")
    ]
)
class CardReplicaEntity(
    @ManyToOne(optional = false)
    var card: CardEntity,
    @Column(nullable = false)
    var userId: Long,
    @Column(name = "card_condition", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var condition: CardCondition,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}