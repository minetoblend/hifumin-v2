package com.minetoblend.osugachabot.cards

interface CardService {
    fun findById(id: CardId): Card?

    fun findByUsername(username: String): Card?

    fun getRandomCards(count: Int): List<Card>

    fun getRandomCardsWithMinimumRarity(count: Int, rarity: CardRarity): List<Card>
}