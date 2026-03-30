package com.minetoblend.osugachabot.daily.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface DailyStreakRepository : JpaRepository<DailyStreakEntity, Long> {
    fun findByUserId(userId: Long): DailyStreakEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DailyStreakEntity s WHERE s.userId = :userId")
    fun findByUserIdForUpdate(userId: Long): DailyStreakEntity?
}
