package com.minetoblend.osugachabot.drops.persistence

import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "dropped_cards")
class DroppedCardEntity(
    @ManyToOne(optional = false)
    var drop: DropEntity,
    @Column(nullable = false)
    var cardIndex: Int,
    @ManyToOne(optional = false)
    var card: CardEntity,
    @Column(name = "card_condition", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var condition: CardCondition,
) {
    @Id
    @GeneratedValue
    var id: Long = 0

    @Column(nullable = true)
    var claimedByUserId: Long? = null
}
