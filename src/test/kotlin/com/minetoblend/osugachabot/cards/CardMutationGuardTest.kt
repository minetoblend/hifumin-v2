package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.tournament.TournamentStatus
import com.minetoblend.osugachabot.tournament.persistence.TournamentEntity
import com.minetoblend.osugachabot.tournament.persistence.TournamentEntryEntity
import com.minetoblend.osugachabot.tournament.persistence.TournamentEntryRepository
import com.minetoblend.osugachabot.tournament.persistence.TournamentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class CardMutationGuardTest {

    @Autowired
    private lateinit var cardMutationGuard: CardMutationGuard

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var cardReplicaRepository: CardReplicaRepository

    @Autowired
    private lateinit var tournamentRepository: TournamentRepository

    @Autowired
    private lateinit var tournamentEntryRepository: TournamentEntryRepository

    private fun saveCard(): CardEntity =
        cardRepository.save(CardEntity(0L, "TestUser", "US", null, 100, 50))

    @Test
    fun `returns Allowed for card not in any tournament`() {
        val card = saveCard()
        val replica = cardReplicaRepository.save(CardReplicaEntity(card, 1L, CardCondition.Mint))

        val result = cardMutationGuard.canMutate(CardReplicaId(replica.id))

        assertEquals(MutationCheck.Allowed, result)
    }

    @Test
    fun `returns InTournament for card in an open tournament`() {
        val card = saveCard()
        val replica = cardReplicaRepository.save(CardReplicaEntity(card, 2L, CardCondition.Mint))

        val tournament = tournamentRepository.save(TournamentEntity().apply {
            name = "Test Tournament"
            status = TournamentStatus.OPEN
        })
        tournamentEntryRepository.save(TournamentEntryEntity(
            tournament = tournament,
            userId = 2L,
            cardReplicaId = replica.id,
            channelId = 100L,
        ))

        val result = cardMutationGuard.canMutate(CardReplicaId(replica.id))

        assertIs<MutationCheck.Blocked>(result)
        assertEquals("Card is entered in a tournament", result.reason)
    }

    @Test
    fun `returns Allowed for card in a resolved tournament`() {
        val card = saveCard()
        val replica = cardReplicaRepository.save(CardReplicaEntity(card, 3L, CardCondition.Mint))

        val tournament = tournamentRepository.save(TournamentEntity().apply {
            name = "Resolved Tournament"
            status = TournamentStatus.RESOLVED
        })
        tournamentEntryRepository.save(TournamentEntryEntity(
            tournament = tournament,
            userId = 3L,
            cardReplicaId = replica.id,
            channelId = 100L,
        ))

        val result = cardMutationGuard.canMutate(CardReplicaId(replica.id))

        assertEquals(MutationCheck.Allowed, result)
    }
}
