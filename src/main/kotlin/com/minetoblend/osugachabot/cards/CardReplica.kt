package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.cards.CardCondition.*
import com.minetoblend.osugachabot.users.UserId
import kotlin.math.roundToInt
import kotlin.math.sqrt

@JvmInline
value class CardReplicaId(val value: Long) {
    fun toDisplayId(): String {
        var remaining = value + OFFSET
        val chars = mutableListOf<Char>()
        while (remaining > 0) {
            chars.add('a' + ((remaining - 1) % 26).toInt())
            remaining = (remaining - 1) / 26
        }
        return chars.reversed().joinToString("")
    }

    companion object {
        private const val OFFSET = 26L + 26 * 26 + 26 * 26 * 26 // skip 1–3 letter range

        fun fromDisplayId(s: String): CardReplicaId? {
            if (s.isEmpty() || s.any { it !in 'a'..'z' }) return null
            var bijectiveValue = 0L
            for (char in s) bijectiveValue = bijectiveValue * 26 + (char - 'a' + 1)
            val id = bijectiveValue - OFFSET
            if (id <= 0) return null
            return CardReplicaId(id)
        }

        fun String.toCardReplicaIdOrNull(): CardReplicaId? = fromDisplayId(this)
    }
}

data class CardReplica(
    val id: CardReplicaId,
    val card: Card,
    val userId: UserId,
    val condition: CardCondition,
)

val CardReplica.burnValue: Int
    get() {
        val multiplier = when (condition) {
            Mint -> 1f
            Good -> 0.5f
            Poor -> 0.2f
            Damaged -> 0.1f
        }

        val baseValue = 3 * sqrt(card.followerCount.toFloat())

        val cardValue = (50 + baseValue) * multiplier

        return cardValue.roundToInt().coerceAtLeast(1)
    }
