package com.minetoblend.osugachabot.wishlist.persistence

import com.minetoblend.osugachabot.cards.persistence.CardEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "wishlisted_cards",
    indexes = [
        Index("idx_wishlist_user_id", "user_id"),
        Index("idx_wishlist_card_id", "card_id"),
    ],
    uniqueConstraints = [UniqueConstraint(name = "uq_wishlist_user_card", columnNames = ["user_id", "card_id"])]
)
class WishlistEntity(
    @Column(nullable = false)
    var userId: Long,
    @ManyToOne(optional = false)
    var card: CardEntity,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}
