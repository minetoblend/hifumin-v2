package com.minetoblend.osugachabot.cards

interface CardService {
    fun findById(id: CardId): Card?
}