package com.minetoblend.osugachabot.tournament.application

import com.minetoblend.osugachabot.cards.*
import com.minetoblend.osugachabot.cards.application.computeBurnValue
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.tournament.*
import com.minetoblend.osugachabot.tournament.TournamentNameGenerator
import com.minetoblend.osugachabot.tournament.persistence.*
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TournamentServiceImpl(
    private val tournamentRepository: TournamentRepository,
    private val tournamentEntryRepository: TournamentEntryRepository,
    private val tournamentPlacementRepository: TournamentPlacementRepository,
    private val cardReplicaRepository: CardReplicaRepository,
    private val cardService: CardService,
    private val cardRepository: CardRepository,
    private val inventoryService: InventoryService,
) : TournamentService {

    @Transactional(readOnly = true)
    override fun getActiveTournament(): Tournament? {
        return tournamentRepository.findFirstByStatusOrderByCreatedAtDesc(TournamentStatus.OPEN)?.toDomain()
    }

    @Transactional
    override fun enterTournament(
        userId: UserId,
        cardReplicaId: CardReplicaId,
        channelId: Long,
        guildId: Long?,
    ): EnterTournamentResult {
        val tournament = tournamentRepository.findFirstByStatusOrderByCreatedAtDesc(TournamentStatus.OPEN)
            ?: return EnterTournamentResult.NoActiveTournament

        val existing = tournamentEntryRepository.findByTournamentIdAndUserId(tournament.id, userId.value)
        if (existing != null) return EnterTournamentResult.AlreadyEntered

        val replica = cardReplicaRepository.findById(cardReplicaId.value).orElse(null)
        if (replica == null || replica.userId != userId.value) return EnterTournamentResult.CardNotOwned

        val weight = computeTournamentWeight(replica.card.followerCount, replica.condition, replica.foil)

        val entry = TournamentEntryEntity(
            tournament = tournament,
            userId = userId.value,
            cardReplicaId = cardReplicaId.value,
            channelId = channelId,
            guildId = guildId,
            weight = weight,
        )

        val savedEntry = tournamentEntryRepository.save(entry)
        tournament.entries.add(savedEntry)

        return EnterTournamentResult.Entered(tournament.toDomain(), savedEntry.toDomain(tournament.id))
    }

    @Transactional
    override fun resolveTournament(tournamentId: TournamentId): TournamentResolution? {
        val tournament = tournamentRepository.findById(tournamentId.value).orElse(null) ?: return null
        if (tournament.status != TournamentStatus.OPEN) return null

        val entries = tournamentEntryRepository.findByTournamentId(tournament.id)

        tournament.status = TournamentStatus.RESOLVED
        tournament.resolvedAt = Instant.now()

        if (entries.isEmpty()) {
            tournamentRepository.save(tournament)
            return TournamentResolution(
                tournament = tournament.toDomain(),
                placements = emptyList(),
            )
        }

        val winner = pickWeightedRandom(entries)

        // Award gold
        inventoryService.addItems(winner.userId.toUserId(), ItemType.Gold, PRIZE_GOLD)

        // Award SSR+ card
        val prizeCards = cardService.getRandomCardsWithMinimumRarity(1, CardRarity.SSR)
        val prizeCard = prizeCards.first()
        val prizeReplica = cardReplicaRepository.save(
            CardReplicaEntity(
                card = cardRepository.getReferenceById(prizeCard.id.value),
                userId = winner.userId,
                condition = CardCondition.Mint,
                burnValue = computeBurnValue(prizeCard.followerCount, CardCondition.Mint),
                foil = false,
            )
        )

        val placement = tournamentPlacementRepository.save(
            TournamentPlacementEntity(
                tournament = tournament,
                place = 1,
                userId = winner.userId,
                prizeGold = PRIZE_GOLD,
                prizeCardReplicaId = prizeReplica.id,
            )
        )
        tournament.placements.add(placement)

        tournamentRepository.save(tournament)

        return TournamentResolution(
            tournament = tournament.toDomain(),
            placements = tournament.placements.map { it.toDomain(tournament.id) },
        )
    }

    @Transactional
    override fun ensureActiveTournament(): Tournament {
        val existing = tournamentRepository.findFirstByStatusOrderByCreatedAtDesc(TournamentStatus.OPEN)
        if (existing != null) return existing.toDomain()

        val tournament = TournamentEntity().apply {
            name = TournamentNameGenerator.generate()
        }
        return tournamentRepository.save(tournament).toDomain()
    }

    private fun pickWeightedRandom(entries: List<TournamentEntryEntity>): TournamentEntryEntity {
        val totalWeight = entries.sumOf { it.weight }
        var random = Math.random() * totalWeight
        for (entry in entries) {
            random -= entry.weight
            if (random <= 0) return entry
        }
        return entries.last()
    }

    private fun TournamentEntity.toDomain() = Tournament(
        id = TournamentId(id),
        name = name,
        status = status,
        createdAt = createdAt,
        resolvedAt = resolvedAt,
        entries = entries.map { it.toDomain(id) },
        placements = placements.map { it.toDomain(id) },
    )

    private fun TournamentEntryEntity.toDomain(tournamentId: Long) = TournamentEntry(
        id = id,
        tournamentId = TournamentId(tournamentId),
        userId = userId.toUserId(),
        cardReplicaId = CardReplicaId(cardReplicaId),
        channelId = channelId,
        guildId = guildId,
        weight = weight,
    )

    private fun TournamentPlacementEntity.toDomain(tournamentId: Long) = TournamentPlacement(
        id = id,
        tournamentId = TournamentId(tournamentId),
        place = place,
        userId = userId.toUserId(),
        prizeGold = prizeGold,
        prizeCardReplicaId = prizeCardReplicaId?.let { CardReplicaId(it) },
    )

    companion object {
        const val PRIZE_GOLD = 1000L

        fun computeTournamentWeight(followerCount: Int, condition: CardCondition, foil: Boolean): Double {
            val conditionMultiplier = when (condition) {
                CardCondition.Mint -> 1.0
                CardCondition.Good -> 0.75
                CardCondition.Poor -> 0.5
                CardCondition.Damaged -> 0.25
            }
            val foilMultiplier = if (foil) 1.5 else 1.0
            return followerCount * conditionMultiplier * foilMultiplier
        }
    }
}
