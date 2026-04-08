package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.tournament.application.TournamentServiceImpl.Companion.computeTournamentWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TournamentWeightTest {

    @Test
    fun `mint condition gives full weight`() {
        val weight = computeTournamentWeight(1000, CardCondition.Mint, false)
        assertEquals(1000.0, weight)
    }

    @Test
    fun `good condition gives 75 percent weight`() {
        val weight = computeTournamentWeight(1000, CardCondition.Good, false)
        assertEquals(750.0, weight)
    }

    @Test
    fun `poor condition gives 50 percent weight`() {
        val weight = computeTournamentWeight(1000, CardCondition.Poor, false)
        assertEquals(500.0, weight)
    }

    @Test
    fun `damaged condition gives 25 percent weight`() {
        val weight = computeTournamentWeight(1000, CardCondition.Damaged, false)
        assertEquals(250.0, weight)
    }

    @Test
    fun `foil gives 1_5x multiplier`() {
        val normal = computeTournamentWeight(1000, CardCondition.Mint, false)
        val foil = computeTournamentWeight(1000, CardCondition.Mint, true)
        assertEquals(normal * 1.5, foil)
    }

    @Test
    fun `higher follower count gives higher weight`() {
        val low = computeTournamentWeight(100, CardCondition.Mint, false)
        val high = computeTournamentWeight(8000, CardCondition.Mint, false)
        assertTrue(high > low)
    }
}
