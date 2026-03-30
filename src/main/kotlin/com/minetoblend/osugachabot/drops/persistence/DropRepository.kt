package com.minetoblend.osugachabot.drops.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface DropRepository : JpaRepository<DropEntity, Long> {
    fun findTopByCreatedByUserIdOrderByCreatedAtDesc(createdByUserId: Long): DropEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DropEntity d WHERE d.id = :id")
    fun findByIdForUpdate(id: Long): DropEntity?
}
