package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.CardRarity
import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.users.UserId

data class TournamentBracket(
    val rounds: List<TournamentRound>,
    val winnerId: Long?,
    val winner: TournamentMatchEntry? = null,
)

data class TournamentRound(
    val matches: List<TournamentMatch>,
)

data class TournamentMatch(
    val entry1: TournamentMatchEntry?,
    val entry2: TournamentMatchEntry?,
    val winnerId: Long,
)

data class TournamentMatchEntry(
    val userId: Long,
    val cardReplica: SnapshotCardReplica,
    val weight: Double,
)

data class SnapshotCardReplica(
    val id: Long,
    val card: SnapshotCard,
    val condition: CardCondition,
    val foil: Boolean,
)

data class SnapshotCard(
    val id: Long,
    val userId: Long,
    val username: String,
    val countryCode: String,
    val title: String?,
    val followerCount: Int,
    val globalRank: Int?,
    val rarity: CardRarity,
)

fun CardReplica.toSnapshot() = SnapshotCardReplica(
    id = id.value,
    card = card.toSnapshot(),
    condition = condition,
    foil = foil,
)

fun Card.toSnapshot() = SnapshotCard(
    id = id.value,
    userId = userId,
    username = username,
    countryCode = countryCode,
    title = title,
    followerCount = followerCount,
    globalRank = globalRank,
    rarity = rarity,
)

fun SnapshotCard.toDomain() = Card(
    id = CardId(id),
    userId = userId,
    username = username,
    countryCode = countryCode,
    title = title,
    followerCount = followerCount,
    globalRank = globalRank,
    rarity = rarity,
)
