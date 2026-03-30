package com.minetoblend.osugachabot.cooldown.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CooldownRepository : JpaRepository<CooldownEntity, Long> {
    fun findByUserIdAndType(userId: Long, type: String): CooldownEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CooldownEntity c WHERE c.userId = :userId AND c.type = :type")
    fun findByUserIdAndTypeForUpdate(userId: Long, type: String): CooldownEntity?

    @Modifying
    @Query(
        value = "INSERT INTO cooldowns (user_id, type, last_used_at) VALUES (:userId, :type, '1970-01-01 00:00:00') ON DUPLICATE KEY UPDATE id = id",
        nativeQuery = true,
    )
    fun insertIgnore(userId: Long, type: String)
}
