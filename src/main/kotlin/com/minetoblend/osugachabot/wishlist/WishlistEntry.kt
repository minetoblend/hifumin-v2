package com.minetoblend.osugachabot.wishlist

import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.users.UserId
import java.time.Instant

@JvmInline
value class WishlistId(val value: Long)

data class WishlistEntry(
    val id: WishlistId,
    val userId: UserId,
    val cardId: CardId,
    val createdAt: Instant,
)
