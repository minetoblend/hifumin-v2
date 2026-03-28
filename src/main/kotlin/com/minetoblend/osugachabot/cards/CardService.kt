package com.minetoblend.osugachabot.cards

interface CardService {
    fun findById(id: CardId): Card?

    fun getRandomCards(count: Int): List<Card>
}