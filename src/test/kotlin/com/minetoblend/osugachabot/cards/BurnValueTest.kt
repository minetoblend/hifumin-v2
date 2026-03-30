package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.cards.application.computeBurnValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BurnValueTest {

    @Test
    fun `foil cards have double burn value`() {
        val followerCount = 10000
        val condition = CardCondition.Mint

        val normalValue = computeBurnValue(followerCount, condition, foil = false)
        val foilValue = computeBurnValue(followerCount, condition, foil = true)

        assertEquals(normalValue * 2, foilValue)
    }

    @Test
    fun `foil multiplier applies on top of condition multiplier`() {
        val followerCount = 10000

        for (condition in CardCondition.entries) {
            val normalValue = computeBurnValue(followerCount, condition, foil = false)
            val foilValue = computeBurnValue(followerCount, condition, foil = true)

            assertEquals(normalValue * 2, foilValue, "Foil should be 2x for condition $condition")
        }
    }
}
