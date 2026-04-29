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
import kotlin.random.Random

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

        val matchEntries = entries.map { it.toMatchEntry() }
        val bracket = simulateBracket(matchEntries)
        tournament.bracket = bracket

        val winnerId = bracket.winnerId!!
        val winnerEntry = entries.first { it.userId == winnerId }

        // Award gold
        inventoryService.addItems(winnerEntry.userId.toUserId(), ItemType.Gold, PRIZE_GOLD)

        // Award SSR+ card
        val prizeCards = cardService.getRandomCardsWithMinimumRarity(1, CardRarity.SSR)
        val prizeCard = prizeCards.first()
        val prizeReplica = cardReplicaRepository.save(
            CardReplicaEntity(
                card = cardRepository.getReferenceById(prizeCard.id.value),
                userId = winnerEntry.userId,
                condition = CardCondition.Mint,
                burnValue = computeBurnValue(prizeCard.followerCount, CardCondition.Mint),
                foil = false,
            )
        )

        val placement = tournamentPlacementRepository.save(
            TournamentPlacementEntity(
                tournament = tournament,
                place = 1,
                userId = winnerEntry.userId,
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

    @Transactional(readOnly = true)
    override fun buildPreviewBracket(tournament: Tournament, viewerUserId: UserId): TournamentBracket {
        val entries = tournament.entries
        val viewerEntry = entries.find { it.userId == viewerUserId }
        val viewerSnapshot = viewerEntry?.cardReplicaId?.let { replicaId ->
            cardReplicaRepository.findById(replicaId.value).orElse(null)?.let { replicaEntity ->
                val cardEntity = replicaEntity.card
                val card = Card(
                    id = CardId(cardEntity.id),
                    userId = cardEntity.userId,
                    username = cardEntity.username,
                    countryCode = cardEntity.countryCode,
                    title = cardEntity.title,
                    followerCount = cardEntity.followerCount,
                    globalRank = cardEntity.globalRank,
                    rarity = cardEntity.rarity,
                )
                CardReplica(
                    id = CardReplicaId(replicaEntity.id),
                    card = card,
                    userId = replicaEntity.userId.toUserId(),
                    condition = replicaEntity.condition,
                    foil = replicaEntity.foil,
                ).toSnapshot()
            }
        }
        return buildPreviewBracket(entries, viewerUserId, viewerSnapshot)
    }

    private fun TournamentEntryEntity.toMatchEntry(): TournamentMatchEntry {
        val replicaEntity = cardReplicaRepository.findById(cardReplicaId!!).orElseThrow()
        val cardEntity = replicaEntity.card
        val card = Card(
            id = CardId(cardEntity.id),
            userId = cardEntity.userId,
            username = cardEntity.username,
            countryCode = cardEntity.countryCode,
            title = cardEntity.title,
            followerCount = cardEntity.followerCount,
            globalRank = cardEntity.globalRank,
            rarity = cardEntity.rarity,
        )
        val replica = CardReplica(
            id = CardReplicaId(replicaEntity.id),
            card = card,
            userId = userId.toUserId(),
            condition = replicaEntity.condition,
            foil = replicaEntity.foil,
        )
        return TournamentMatchEntry(
            userId = userId,
            cardReplica = replica.toSnapshot(),
            weight = weight,
        )
    }

    private fun TournamentEntity.toDomain() = Tournament(
        id = TournamentId(id),
        name = name,
        status = status,
        createdAt = createdAt,
        resolvedAt = resolvedAt,
        bracket = bracket,
        entries = entries.map { it.toDomain(id) },
        placements = placements.map { it.toDomain(id) },
    )

    private fun TournamentEntryEntity.toDomain(tournamentId: Long) = TournamentEntry(
        id = id,
        tournamentId = TournamentId(tournamentId),
        userId = userId.toUserId(),
        cardReplicaId = cardReplicaId?.let { CardReplicaId(it) },
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

        private const val NO_WINNER_ID = Long.MIN_VALUE

        fun buildPreviewBracket(
            entries: List<TournamentEntry>,
            viewerUserId: UserId,
            viewerSnapshot: SnapshotCardReplica?,
        ): TournamentBracket {
            if (entries.isEmpty()) return TournamentBracket(rounds = emptyList(), winnerId = null)

            val seeded = entries.sortedByDescending { it.weight }
            val bracketSize = Integer.highestOneBit(seeded.size - 1).shl(1).coerceAtLeast(2)
            val slots: List<TournamentEntry?> = seeded + List(bracketSize - seeded.size) { null }

            val firstRoundMatches = mutableListOf<TournamentMatch>()
            for (i in slots.indices step 2) {
                val e1 = slots[i]
                val e2 = slots[i + 1]
                val me1 = e1?.toPreviewMatchEntry(viewerUserId, viewerSnapshot)
                val me2 = e2?.toPreviewMatchEntry(viewerUserId, viewerSnapshot)
                if (me1 != null || me2 != null) {
                    firstRoundMatches.add(TournamentMatch(entry1 = me1, entry2 = me2, winnerId = NO_WINNER_ID))
                }
            }

            if (firstRoundMatches.isEmpty()) return TournamentBracket(rounds = emptyList(), winnerId = null)

            val rounds = mutableListOf(TournamentRound(firstRoundMatches))
            var currentMatchCount = firstRoundMatches.size
            var nextId = -1L
            while (currentMatchCount > 1) {
                val nextMatchCount = currentMatchCount / 2
                val placeholderMatches = (0 until nextMatchCount).map {
                    TournamentMatch(
                        entry1 = anonymousMatchEntry(nextId--),
                        entry2 = anonymousMatchEntry(nextId--),
                        winnerId = NO_WINNER_ID,
                    )
                }
                rounds.add(TournamentRound(placeholderMatches))
                currentMatchCount = nextMatchCount
            }

            return TournamentBracket(rounds = rounds, winnerId = null, winner = null)
        }

        private fun TournamentEntry.toPreviewMatchEntry(
            viewerUserId: UserId,
            viewerSnapshot: SnapshotCardReplica?,
        ): TournamentMatchEntry {
            val snapshot = if (userId == viewerUserId) viewerSnapshot else null
            return TournamentMatchEntry(userId = userId.value, cardReplica = snapshot, weight = weight)
        }

        private fun anonymousMatchEntry(id: Long) = TournamentMatchEntry(
            userId = id,
            cardReplica = null,
            weight = 0.0,
        )

        fun computeTournamentWeight(followerCount: Int, condition: CardCondition, foil: Boolean): Double {
            val conditionMultiplier = when (condition) {
                Mint -> 1.0
                Good -> 0.75
                Poor -> 0.5
                Damaged -> 0.25
            }
            val foilMultiplier = if (foil) 1.5 else 1.0
            return followerCount * conditionMultiplier * foilMultiplier
        }

        fun simulateBracket(
            entries: List<TournamentMatchEntry>,
            random: Random = Random,
        ): TournamentBracket {
            if (entries.size == 1) {
                val entry = entries.first()
                return TournamentBracket(
                    rounds = listOf(
                        TournamentRound(
                            listOf(
                                TournamentMatch(
                                    entry1 = entry,
                                    entry2 = null,
                                    winnerId = entry.userId,
                                )
                            )
                        )
                    ),
                    winnerId = entry.userId,
                    winner = entry,
                )
            }

            // Seed by weight descending so strongest face weakest
            val seeded = entries.sortedByDescending { it.weight }

            // Pad to next power of 2 with byes (null entries)
            val bracketSize = Integer.highestOneBit(seeded.size - 1).shl(1).coerceAtLeast(2)
            val slots: List<TournamentMatchEntry?> = seeded + List(bracketSize - seeded.size) { null }

            val rounds = mutableListOf<TournamentRound>()
            var currentRound = slots

            while (currentRound.size > 1) {
                val matches = mutableListOf<TournamentMatch>()
                val nextRound = mutableListOf<TournamentMatchEntry?>()

                for (i in currentRound.indices step 2) {
                    val e1 = currentRound[i]
                    val e2 = currentRound[i + 1]

                    val winner = when {
                        e1 == null && e2 == null -> null
                        e1 == null -> e2
                        e2 == null -> e1
                        else -> simulateMatch(e1, e2, random)
                    }

                    if (e1 != null || e2 != null) {
                        matches.add(
                            TournamentMatch(
                                entry1 = e1,
                                entry2 = e2,
                                winnerId = winner!!.userId,
                            )
                        )
                    }

                    nextRound.add(winner)
                }

                if (matches.isNotEmpty()) {
                    rounds.add(TournamentRound(matches))
                }
                currentRound = nextRound
            }

            val bracketWinner = currentRound.firstOrNull()
            return TournamentBracket(
                rounds = rounds,
                winnerId = bracketWinner?.userId,
                winner = bracketWinner,
            )
        }

        private fun simulateMatch(
            e1: TournamentMatchEntry,
            e2: TournamentMatchEntry,
            random: Random,
        ): TournamentMatchEntry {
            val totalWeight = e1.weight + e2.weight
            return if (random.nextDouble() * totalWeight < e1.weight) e1 else e2
        }
    }
}
