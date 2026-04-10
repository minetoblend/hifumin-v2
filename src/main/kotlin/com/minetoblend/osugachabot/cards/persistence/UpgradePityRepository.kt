package com.minetoblend.osugachabot.cards.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UpgradePityRepository : JpaRepository<UpgradePityEntity, UpgradePityId> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM UpgradePityEntity p WHERE p.id = :id")
    fun findByIdForUpdate(@Param("id") id: UpgradePityId): UpgradePityEntity?
}
