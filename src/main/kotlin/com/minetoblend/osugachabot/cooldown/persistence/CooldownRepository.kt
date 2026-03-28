package com.minetoblend.osugachabot.cooldown.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CooldownRepository : JpaRepository<CooldownEntity, Long> {
    fun findByUserIdAndType(userId: Long, type: String): CooldownEntity?
}
