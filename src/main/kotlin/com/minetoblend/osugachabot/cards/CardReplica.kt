package com.minetoblend.osugachabot.cards

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
    }
}

data class CardReplica(
    val id: CardReplicaId,
    val card: Card,
    val userId: Long,
    val condition: CardCondition,
)
