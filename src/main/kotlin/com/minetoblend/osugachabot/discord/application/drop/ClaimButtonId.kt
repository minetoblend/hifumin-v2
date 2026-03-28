package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.discord.ButtonId
import com.minetoblend.osugachabot.discord.ButtonIdCompanion
import com.minetoblend.osugachabot.drops.DropId
import com.minetoblend.osugachabot.drops.DropId.Companion.toDropId

data class ClaimButtonId(
    val dropId: DropId,
    val cardIndex: Int,
) : ButtonId {
    companion object : ButtonIdCompanion<ClaimButtonId> {
        private const val PREFIX = "drop:claim:"

        private val regex = Regex("$PREFIX(\\d+):(\\d+)")

        override fun isValid(id: String): Boolean = id.startsWith(PREFIX)

        override fun fromString(id: String): ClaimButtonId? {
            val match = regex.matchEntire(id) ?: return null

            return ClaimButtonId(
                dropId = match.groupValues[1].toDropId(),
                cardIndex = match.groupValues[2].toInt(),
            )
        }
    }

    override fun toCustomId(): String = "$PREFIX${dropId.value}:${cardIndex}"
}