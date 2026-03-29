package com.minetoblend.osugachabot.cards.application

import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.cards.CardReplicaService
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CardReplicaServiceImpl(
    private val cardReplicaRepository: CardReplicaRepository,
) : CardReplicaService {
    override fun findById(id: CardReplicaId): CardReplica? =
        cardReplicaRepository.findByIdOrNull(id.value)?.toDomain()

    private fun CardReplicaEntity.toDomain() = CardReplica(
        id = CardReplicaId(id),
        card = card.toDomain(),
        userId = userId,
        condition = condition,
    )

    private fun CardEntity.toDomain() = Card(
        id = CardId(id),
        userId = userId,
        username = username,
        countryCode = countryCode,
        title = title,
        followerCount = followerCount,
        globalRank = globalRank,
        rarity = rarity,
    )
}
