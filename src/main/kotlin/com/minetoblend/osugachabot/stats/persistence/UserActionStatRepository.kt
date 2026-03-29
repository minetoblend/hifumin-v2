package com.minetoblend.osugachabot.stats.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserActionStatRepository : JpaRepository<UserActionStatEntity, UserActionStatId> {
    fun findByIdUserId(userId: Long): List<UserActionStatEntity>
}
