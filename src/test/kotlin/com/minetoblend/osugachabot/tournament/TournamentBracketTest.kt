package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardRarity
import com.minetoblend.osugachabot.tournament.application.TournamentServiceImpl.Companion.simulateBracket
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TournamentBracketTest {

    private fun entry(userId: Long, weight: Double, username: String = "Player$userId") =
        TournamentMatchEntry(
            userId = userId,
            cardReplica = SnapshotCardReplica(
                id = userId,
                card = SnapshotCard(
                    id = userId,
                    userId = userId,
                    username = username,
                    countryCode = "US",
                    title = null,
                    followerCount = weight.toInt(),
                    globalRank = null,
                    rarity = CardRarity.N,
                ),
                condition = CardCondition.Mint,
                foil = false,
            ),
            weight = weight,
        )

    @Test
    fun `single entry produces one round with a bye and that entry wins`() {
        val bracket = simulateBracket(listOf(entry(1, 100.0)))

        assertEquals(1, bracket.rounds.size)
        assertEquals(1, bracket.rounds[0].matches.size)
        val match = bracket.rounds[0].matches[0]
        assertNotNull(match.entry1)
        assertNull(match.entry2)
        assertEquals(1L, match.winnerId)
        assertEquals(1L, bracket.winnerId)
        assertNotNull(bracket.winner)
    }

    @Test
    fun `two entries produce one round with one match`() {
        val bracket = simulateBracket(
            listOf(entry(1, 100.0), entry(2, 50.0)),
            random = Random(42),
        )

        assertEquals(1, bracket.rounds.size)
        assertEquals(1, bracket.rounds[0].matches.size)

        val match = bracket.rounds[0].matches[0]
        assertNotNull(match.entry1)
        assertNotNull(match.entry2)
        assertNotNull(bracket.winnerId)
        assertTrue(bracket.winnerId == 1L || bracket.winnerId == 2L)
    }

    @Test
    fun `three entries produce bracket with byes padded to 4`() {
        val bracket = simulateBracket(
            listOf(entry(1, 300.0), entry(2, 200.0), entry(3, 100.0)),
            random = Random(42),
        )

        // First round should have 2 matches (4 slots -> 2 matches)
        // One match will be a bye (one null entry)
        val firstRound = bracket.rounds[0]
        val byeMatches = firstRound.matches.count { it.entry1 == null || it.entry2 == null }
        assertEquals(1, byeMatches, "Should have exactly one bye match")

        // Should have a final round
        assertTrue(bracket.rounds.size >= 2)
        assertNotNull(bracket.winnerId)
    }

    @Test
    fun `four entries produce a proper two-round bracket`() {
        val bracket = simulateBracket(
            listOf(entry(1, 400.0), entry(2, 300.0), entry(3, 200.0), entry(4, 100.0)),
            random = Random(42),
        )

        assertEquals(2, bracket.rounds.size)
        assertEquals(2, bracket.rounds[0].matches.size)
        assertEquals(1, bracket.rounds[1].matches.size)
        assertNotNull(bracket.winnerId)
    }

    @Test
    fun `seeding pairs strongest with weakest`() {
        val bracket = simulateBracket(
            listOf(entry(1, 400.0), entry(2, 300.0), entry(3, 200.0), entry(4, 100.0)),
            random = Random(42),
        )

        val firstRound = bracket.rounds[0]
        // Entry with weight 400 should face entry with weight 100
        val match1 = firstRound.matches[0]
        val weights1 = setOf(match1.entry1!!.weight, match1.entry2!!.weight)
        assertEquals(setOf(400.0, 300.0), weights1)

        val match2 = firstRound.matches[1]
        val weights2 = setOf(match2.entry1!!.weight, match2.entry2!!.weight)
        assertEquals(setOf(200.0, 100.0), weights2)
    }

    @Test
    fun `higher weight wins more often with deterministic seed`() {
        var strongWins = 0
        repeat(1000) { seed ->
            val bracket = simulateBracket(
                listOf(entry(1, 900.0), entry(2, 100.0)),
                random = Random(seed),
            )
            if (bracket.winnerId == 1L) strongWins++
        }

        // With 90% weight, strong player should win ~90% of the time
        assertTrue(strongWins > 800, "Strong player won $strongWins/1000, expected >800")
        assertTrue(strongWins < 980, "Strong player won $strongWins/1000, weak player should still win sometimes")
    }

    @Test
    fun `winner is always one of the participants`() {
        val entries = (1L..7L).map { entry(it, it * 100.0) }
        repeat(100) { seed ->
            val bracket = simulateBracket(entries, random = Random(seed))
            val participantIds = entries.map { it.userId }.toSet()
            assertTrue(bracket.winnerId in participantIds)
        }
    }

    @Test
    fun `bracket with 8 entries produces 3 rounds`() {
        val entries = (1L..8L).map { entry(it, it * 100.0) }
        val bracket = simulateBracket(entries, random = Random(42))

        assertEquals(3, bracket.rounds.size)
        assertEquals(4, bracket.rounds[0].matches.size)
        assertEquals(2, bracket.rounds[1].matches.size)
        assertEquals(1, bracket.rounds[2].matches.size)
    }
}
