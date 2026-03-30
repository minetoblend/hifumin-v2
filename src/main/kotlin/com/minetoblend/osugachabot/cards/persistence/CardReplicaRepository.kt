package com.minetoblend.osugachabot.cards.persistence

import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CardReplicaRepository : JpaRepository<CardReplicaEntity, Long> {
    fun findFirstByUserIdOrderByCreatedAtDesc(userId: Long): CardReplicaEntity?

    fun countByUserId(userId: Long): Int

    fun findByUserId(userId: Long, pageable: Pageable): Page<CardReplicaEntity>

    fun findByBurnValueIsNull(): List<CardReplicaEntity>

    @Query("SELECT COALESCE(SUM(r.burnValue), 0) FROM CardReplicaEntity r WHERE r.userId = :userId")
    fun sumBurnValueByUserId(@Param("userId") userId: Long): Long

    @Query("SELECT DISTINCT r.userId FROM CardReplicaEntity r")
    fun findDistinctUserIds(): List<Long>
}