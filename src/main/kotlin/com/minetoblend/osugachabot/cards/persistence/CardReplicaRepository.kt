package com.minetoblend.osugachabot.cards.persistence

import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CardReplicaRepository : JpaRepository<CardReplicaEntity, Long> {
    fun findFirstByUserIdOrderByCreatedAtDesc(userId: Long): CardReplicaEntity?

    fun countByUserId(userId: Long): Int

    fun findByUserId(userId: Long, pageable: Pageable): Page<CardReplicaEntity>

    fun findByBurnValueIsNull(): List<CardReplicaEntity>
}