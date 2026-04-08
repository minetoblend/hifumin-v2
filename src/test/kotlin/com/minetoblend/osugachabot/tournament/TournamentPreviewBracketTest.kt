package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardRarity
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.tournament.application.TournamentServiceImpl
import com.minetoblend.osugachabot.users.UserId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TournamentPreviewBracketTest {

    private val viewerUserId = UserId(1L)

    private fun entry(userId: Long, weight: Double) = TournamentEntry(
        id = userId,
        tournamentId = TournamentId(1L),
        userId = UserId(userId),
        cardReplicaId = CardReplicaId(userId),
        channelId = 1L,
        guildId = null,
        weight = weight,
    )

    private fun viewerSnapshot(osuUserId: Long, username: String = "Viewer") = SnapshotCardReplica(
        id = osuUserId,
        card = SnapshotCard(
            id = osuUserId,
            userId = osuUserId,
            username = username,
            countryCode = "US",
            title = null,
            followerCount = 1000,
            globalRank = 500,
            rarity = CardRarity.SSR,
        ),
        condition = CardCondition.Mint,
        foil = false,
    )

    @Test
    fun `empty entries return empty bracket`() {
        val bracket = TournamentServiceImpl.buildPreviewBracket(emptyList(), viewerUserId, null)

        assertTrue(bracket.rounds.isEmpty())
        assertNull(bracket.winnerId)
        assertNull(bracket.winner)
    }

    @Test
    fun `viewer not entered shows all question marks in first round`() {
        val entries = listOf(entry(2L, 200.0), entry(3L, 100.0))
        val bracket = TournamentServiceImpl.buildPreviewBracket(entries, viewerUserId, null)

        val firstRound = bracket.rounds.first()
        val allEntries = firstRound.matches.flatMap { listOfNotNull(it.entry1, it.entry2) }
        assertTrue(allEntries.all { it.cardReplica.card.username == "???" })
    }

    @Test
    fun `viewer's own card is shown with real data`() {
        val snapshot = viewerSnapshot(100L, "MyPlayer")
        val entries = listOf(entry(1L, 300.0), entry(2L, 200.0), entry(3L, 100.0))
        val bracket = TournamentServiceImpl.buildPreviewBracket(entries, viewerUserId, snapshot)

        val firstRound = bracket.rounds.first()
        val allEntries = firstRound.matches.flatMap { listOfNotNull(it.entry1, it.entry2) }
        val viewerEntry = allEntries.find { it.userId == viewerUserId.value }

        assertNotNull(viewerEntry)
        assertEquals("MyPlayer", viewerEntry.cardReplica.card.username)
    }

    @Test
    fun `non-viewer entries show question marks when viewer is present`() {
        val snapshot = viewerSnapshot(100L)
        val entries = listOf(entry(1L, 300.0), entry(2L, 200.0), entry(3L, 100.0))
        val bracket = TournamentServiceImpl.buildPreviewBracket(entries, viewerUserId, snapshot)

        val firstRound = bracket.rounds.first()
        val allEntries = firstRound.matches.flatMap { listOfNotNull(it.entry1, it.entry2) }
        val nonViewerEntries = allEntries.filter { it.userId != viewerUserId.value }

        assertTrue(nonViewerEntries.isNotEmpty())
        assertTrue(nonViewerEntries.all { it.cardReplica.card.username == "???" })
    }

    @Test
    fun `no winner is displayed in preview`() {
        val entries = listOf(entry(1L, 200.0), entry(2L, 100.0))
        val bracket = TournamentServiceImpl.buildPreviewBracket(entries, viewerUserId, null)

        assertNull(bracket.winnerId)
        assertNull(bracket.winner)
        val allMatches = bracket.rounds.flatMap { it.matches }
        for (match in allMatches) {
            assertTrue(match.entry1?.userId != match.winnerId)
            assertTrue(match.entry2?.userId != match.winnerId)
        }
    }

    @Test
    fun `subsequent rounds have placeholder question mark entries`() {
        val entries = (1L..4L).map { entry(it, it * 100.0) }
        val bracket = TournamentServiceImpl.buildPreviewBracket(entries, UserId(99L), null)

        assertEquals(2, bracket.rounds.size)
        val round2Entries = bracket.rounds[1].matches.flatMap { listOfNotNull(it.entry1, it.entry2) }
        assertTrue(round2Entries.all { it.cardReplica.card.username == "???" })
    }

    @Test
    fun `four entries produce correct bracket structure`() {
        val entries = listOf(entry(1L, 400.0), entry(2L, 300.0), entry(3L, 200.0), entry(4L, 100.0))
        val bracket = TournamentServiceImpl.buildPreviewBracket(entries, UserId(99L), null)

        assertEquals(2, bracket.rounds.size)
        assertEquals(2, bracket.rounds[0].matches.size)
        assertEquals(1, bracket.rounds[1].matches.size)
    }

    @Test
    fun `eight entries produce three-round bracket`() {
        val entries = (1L..8L).map { entry(it, it * 100.0) }
        val bracket = TournamentServiceImpl.buildPreviewBracket(entries, UserId(99L), null)

        assertEquals(3, bracket.rounds.size)
        assertEquals(4, bracket.rounds[0].matches.size)
        assertEquals(2, bracket.rounds[1].matches.size)
        assertEquals(1, bracket.rounds[2].matches.size)
    }

    @Test
    fun `entries are seeded by weight descending`() {
        val entries = listOf(entry(1L, 100.0), entry(2L, 400.0), entry(3L, 200.0), entry(4L, 300.0))
        val bracket = TournamentServiceImpl.buildPreviewBracket(entries, UserId(99L), null)

        val firstRound = bracket.rounds[0]
        // Sorted descending: 400, 300, 200, 100 → pairs: (400,300), (200,100)
        val match1Weights = setOf(firstRound.matches[0].entry1!!.weight, firstRound.matches[0].entry2!!.weight)
        val match2Weights = setOf(firstRound.matches[1].entry1!!.weight, firstRound.matches[1].entry2!!.weight)
        assertEquals(setOf(400.0, 300.0), match1Weights)
        assertEquals(setOf(200.0, 100.0), match2Weights)
    }
}
