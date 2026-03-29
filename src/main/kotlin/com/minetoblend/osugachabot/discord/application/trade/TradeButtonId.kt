package com.minetoblend.osugachabot.discord.application.trade

import com.minetoblend.osugachabot.discord.ButtonId
import com.minetoblend.osugachabot.discord.ButtonIdCompanion
import com.minetoblend.osugachabot.trading.TradeId

data class TradeButtonId(
    val tradeId: TradeId,
    val action: Action,
) : ButtonId {

    enum class Action { Accept, Decline, Cancel }

    companion object : ButtonIdCompanion<TradeButtonId> {
        private const val PREFIX = "trade:"
        private val regex = Regex("$PREFIX(\\d+):(accept|decline|cancel)")

        override fun isValid(id: String): Boolean = id.startsWith(PREFIX)

        override fun fromString(id: String): TradeButtonId? {
            val match = regex.matchEntire(id) ?: return null
            val action = when (match.groupValues[2]) {
                "accept" -> Action.Accept
                "decline" -> Action.Decline
                "cancel" -> Action.Cancel
                else -> return null
            }
            return TradeButtonId(
                tradeId = TradeId(match.groupValues[1].toLong()),
                action = action,
            )
        }
    }

    override fun toCustomId(): String {
        val actionStr = when (action) {
            Action.Accept -> "accept"
            Action.Decline -> "decline"
            Action.Cancel -> "cancel"
        }
        return "$PREFIX${tradeId.value}:$actionStr"
    }
}
