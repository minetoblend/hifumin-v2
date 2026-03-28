package com.minetoblend.osugachabot.cards

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "card_replicas")
class CardReplicaEntity(
    @ManyToOne(optional = false)
    var card: CardEntity,
    @Column(nullable = false)
    var userId: Long,
) {
    @Id
    @GeneratedValue
    var id: Long = 0

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}