package com.minetoblend.osugachabot.cards.persistence

import com.minetoblend.osugachabot.cards.CardCondition
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

    @Query("SELECT r FROM CardReplicaEntity r WHERE r.burnValueVersion IS NULL OR r.burnValueVersion != :version")
    fun findWithStaleBurnValue(@Param("version") version: Int, pageable: Pageable): List<CardReplicaEntity>

    @Query("SELECT COALESCE(SUM(r.burnValue), 0) FROM CardReplicaEntity r WHERE r.userId = :userId")
    fun sumBurnValueByUserId(@Param("userId") userId: Long): Long

    @Query("SELECT DISTINCT r.userId FROM CardReplicaEntity r")
    fun findDistinctUserIds(): List<Long>

    @Query(
        value = "SELECT r.userId as userId, COUNT(r) as count FROM CardReplicaEntity r WHERE r.condition = :condition GROUP BY r.userId ORDER BY COUNT(r) DESC",
        countQuery = "SELECT COUNT(DISTINCT r.userId) FROM CardReplicaEntity r WHERE r.condition = :condition",
    )
    fun countByConditionGroupByUser(@Param("condition") condition: CardCondition, pageable: Pageable): Page<ConditionCountProjection>
}