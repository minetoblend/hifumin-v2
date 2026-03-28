package com.minetoblend.osugachabot.drops

import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.users.UserId

@JvmInline
value class DropId(val value: Long) {
    companion object {
        fun Long.toDropId() = DropId(this)

        fun String.toDropId() = toLong().toDropId()
    }
}

data class DroppedCard(
    val index: Int,
    val card: Card,
    val condition: CardCondition,
    val claimedBy: UserId? = null,
)

data class Drop(
    val id: DropId,
    val cards: List<DroppedCard>,
    val createdAt: java.time.Instant,
)
