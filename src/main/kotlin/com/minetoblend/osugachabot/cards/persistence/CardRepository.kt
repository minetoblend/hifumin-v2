package com.minetoblend.osugachabot.cards.persistence

import com.minetoblend.osugachabot.cards.CardRarity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CardRepository : JpaRepository<CardEntity, Long> {
    @Query("SELECT c FROM CardEntity c ORDER BY RAND() LIMIT :count")
    fun getRandomCards(count: Int): List<CardEntity>

    @Query("SELECT c FROM CardEntity c WHERE c.rarity IN :rarity ORDER BY RAND() LIMIT :count")
    fun getRandomCardsWithRarityIn(count: Int, rarity: List<CardRarity>): List<CardEntity>

    fun findByUsernameIgnoreCase(username: String): CardEntity?
}