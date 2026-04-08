package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class TournamentServiceImplTest {

    @Autowired
    private lateinit var tournamentService: TournamentService

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var cardReplicaRepository: CardReplicaRepository

    @Autowired
    private lateinit var inventoryService: InventoryService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private fun cleanup() {
        jdbcTemplate.update("DELETE FROM tournament_placements")
        jdbcTemplate.update("DELETE FROM tournament_entries")
        jdbcTemplate.update("DELETE FROM tournaments")
    }

    private fun seedCard(followerCount: Int = 1000): CardEntity {
        return cardRepository.save(CardEntity(99L, "TestPlayer", "US", null, followerCount, 1))
    }

    private fun createReplica(userId: UserId, card: CardEntity, condition: CardCondition = CardCondition.Mint): CardReplicaEntity {
        return cardReplicaRepository.save(
            CardReplicaEntity(
                card = card,
                userId = userId.value,
                condition = condition,
                foil = false,
            )
        )
    }

    @Test
    fun `ensureActiveTournament creates a new tournament when none exists`() {
        cleanup()

        val tournament = tournamentService.ensureActiveTournament()

        assertEquals(TournamentStatus.OPEN, tournament.status)
        assertTrue(tournament.id.value > 0)
    }

    @Test
    fun `ensureActiveTournament returns existing tournament when one is open`() {
        cleanup()

        val first = tournamentService.ensureActiveTournament()
        val second = tournamentService.ensureActiveTournament()

        assertEquals(first.id, second.id)
    }

    @Test
    fun `getActiveTournament returns null when no tournament exists`() {
        cleanup()

        assertNull(tournamentService.getActiveTournament())
    }

    @Test
    fun `getActiveTournament returns open tournament`() {
        cleanup()

        val created = tournamentService.ensureActiveTournament()
        val found = tournamentService.getActiveTournament()

        assertNotNull(found)
        assertEquals(created.id, found.id)
    }

    @Test
    fun `enterTournament succeeds with valid card`() {
        cleanup()
        tournamentService.ensureActiveTournament()
        val card = seedCard()
        val replica = createReplica(UserId(1L), card)

        val result = tournamentService.enterTournament(
            userId = UserId(1L),
            cardReplicaId = CardReplicaId(replica.id),
            channelId = 100L,
            guildId = 200L,
        )

        assertIs<EnterTournamentResult.Entered>(result)
        assertEquals(1, result.tournament.entries.size)
    }

    @Test
    fun `enterTournament returns NoActiveTournament when none exists`() {
        cleanup()

        val result = tournamentService.enterTournament(
            userId = UserId(1L),
            cardReplicaId = CardReplicaId(1L),
            channelId = 100L,
            guildId = null,
        )

        assertIs<EnterTournamentResult.NoActiveTournament>(result)
    }

    @Test
    fun `enterTournament returns AlreadyEntered when user enters twice`() {
        cleanup()
        tournamentService.ensureActiveTournament()
        val card = seedCard()
        val replica = createReplica(UserId(1L), card)

        tournamentService.enterTournament(UserId(1L), CardReplicaId(replica.id), 100L, null)
        val result = tournamentService.enterTournament(UserId(1L), CardReplicaId(replica.id), 100L, null)

        assertIs<EnterTournamentResult.AlreadyEntered>(result)
    }

    @Test
    fun `enterTournament returns CardNotOwned for nonexistent card`() {
        cleanup()
        tournamentService.ensureActiveTournament()

        val result = tournamentService.enterTournament(
            userId = UserId(1L),
            cardReplicaId = CardReplicaId(999999L),
            channelId = 100L,
            guildId = null,
        )

        assertIs<EnterTournamentResult.CardNotOwned>(result)
    }

    @Test
    fun `enterTournament returns CardNotOwned when card belongs to another user`() {
        cleanup()
        tournamentService.ensureActiveTournament()
        val card = seedCard()
        val replica = createReplica(UserId(2L), card)

        val result = tournamentService.enterTournament(
            userId = UserId(1L),
            cardReplicaId = CardReplicaId(replica.id),
            channelId = 100L,
            guildId = null,
        )

        assertIs<EnterTournamentResult.CardNotOwned>(result)
    }

    @Test
    fun `resolveTournament picks a winner and creates placement with prizes`() {
        cleanup()
        val tournament = tournamentService.ensureActiveTournament()
        val card = seedCard(1000)
        val replica = createReplica(UserId(1L), card)
        tournamentService.enterTournament(UserId(1L), CardReplicaId(replica.id), 100L, 200L)

        val goldBefore = inventoryService.getItem(UserId(1L), ItemType.Gold).amount

        val resolution = tournamentService.resolveTournament(tournament.id)

        assertNotNull(resolution)
        assertEquals(1, resolution.placements.size)

        val firstPlace = resolution.placements.first()
        assertEquals(1, firstPlace.place)
        assertEquals(UserId(1L), firstPlace.userId)
        assertEquals(1000L, firstPlace.prizeGold)
        assertNotNull(firstPlace.prizeCardReplicaId)

        val goldAfter = inventoryService.getItem(UserId(1L), ItemType.Gold).amount
        assertEquals(goldBefore + 1000L, goldAfter)
    }

    @Test
    fun `resolveTournament with no entries resolves with empty placements`() {
        cleanup()
        val tournament = tournamentService.ensureActiveTournament()

        val resolution = tournamentService.resolveTournament(tournament.id)

        assertNotNull(resolution)
        assertTrue(resolution.placements.isEmpty())
        assertEquals(TournamentStatus.RESOLVED, resolution.tournament.status)
    }

    @Test
    fun `resolveTournament marks tournament as resolved`() {
        cleanup()
        val tournament = tournamentService.ensureActiveTournament()

        tournamentService.resolveTournament(tournament.id)

        val active = tournamentService.getActiveTournament()
        assertNull(active)
    }

    @Test
    fun `resolveTournament returns null for already resolved tournament`() {
        cleanup()
        val tournament = tournamentService.ensureActiveTournament()
        tournamentService.resolveTournament(tournament.id)

        val result = tournamentService.resolveTournament(tournament.id)

        assertNull(result)
    }

    @Test
    fun `tournament weight is higher for better cards`() {
        cleanup()
        tournamentService.ensureActiveTournament()
        val card = seedCard(1000)
        val mintReplica = createReplica(UserId(1L), card, CardCondition.Mint)
        val damagedReplica = createReplica(UserId(2L), card, CardCondition.Damaged)

        val result1 = tournamentService.enterTournament(UserId(1L), CardReplicaId(mintReplica.id), 100L, null)
        val result2 = tournamentService.enterTournament(UserId(2L), CardReplicaId(damagedReplica.id), 100L, null)

        assertIs<EnterTournamentResult.Entered>(result1)
        assertIs<EnterTournamentResult.Entered>(result2)
        assertTrue(result1.entry.weight > result2.entry.weight)
    }

    @Test
    fun `multiple users can enter the same tournament`() {
        cleanup()
        tournamentService.ensureActiveTournament()
        val card = seedCard()
        val replica1 = createReplica(UserId(1L), card)
        val replica2 = createReplica(UserId(2L), card)
        val replica3 = createReplica(UserId(3L), card)

        val r1 = tournamentService.enterTournament(UserId(1L), CardReplicaId(replica1.id), 100L, null)
        val r2 = tournamentService.enterTournament(UserId(2L), CardReplicaId(replica2.id), 100L, null)
        val r3 = tournamentService.enterTournament(UserId(3L), CardReplicaId(replica3.id), 100L, null)

        assertIs<EnterTournamentResult.Entered>(r1)
        assertIs<EnterTournamentResult.Entered>(r2)
        assertIs<EnterTournamentResult.Entered>(r3)

        val tournament = tournamentService.getActiveTournament()
        assertNotNull(tournament)
        assertEquals(3, tournament.entries.size)
    }

    @Test
    fun `resolved tournament placements are persisted and visible on tournament domain`() {
        cleanup()
        val tournament = tournamentService.ensureActiveTournament()
        val card = seedCard(1000)
        val replica = createReplica(UserId(1L), card)
        tournamentService.enterTournament(UserId(1L), CardReplicaId(replica.id), 100L, null)

        val resolution = tournamentService.resolveTournament(tournament.id)

        assertNotNull(resolution)
        assertEquals(1, resolution.tournament.placements.size)
        assertEquals(1, resolution.tournament.placements.first().place)
    }
}
