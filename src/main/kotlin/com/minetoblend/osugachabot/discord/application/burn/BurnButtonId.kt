package com.minetoblend.osugachabot.discord.application.burn

import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.discord.ButtonId
import com.minetoblend.osugachabot.discord.ButtonIdCompanion

data class BurnButtonId(val replicaId: CardReplicaId) : ButtonId {
    companion object : ButtonIdCompanion<BurnButtonId> {
        private val regex = Regex("card:burn:(\\d+)")

        override fun isValid(id: String) = regex.matches(id)

        override fun fromString(id: String): BurnButtonId? {
            val match = regex.matchEntire(id) ?: return null
            return BurnButtonId(CardReplicaId(match.groupValues[1].toLong()))
        }
    }

    override fun toCustomId() = "card:burn:${replicaId.value}"
}
