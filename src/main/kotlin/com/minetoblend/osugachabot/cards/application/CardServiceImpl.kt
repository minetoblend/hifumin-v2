package com.minetoblend.osugachabot.cards.application

import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.CardService
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CardServiceImpl(
    private val cards: CardRepository
) : CardService {
    override fun findById(id: CardId): Card? =
        cards.findByIdOrNull(id.value)?.toDomain()

    override fun getRandomCards(count: Int): List<Card> =
        cards.getRandomCards(count).map { it.toDomain() }

    private fun CardEntity.toDomain(): Card = Card(
        CardId(id),
        userId,
        username,
        countryCode,
        title,
        followerCount,
        globalRank
    )
}