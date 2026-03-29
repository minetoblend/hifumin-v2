package com.minetoblend.osugachabot.cards.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CardRepository : JpaRepository<CardEntity, Long> {
    @Query("SELECT c FROM CardEntity c ORDER BY RAND() LIMIT :count")
    fun getRandomCards(count: Int): List<CardEntity>

    fun findByUsernameIgnoreCase(username: String): CardEntity?
}