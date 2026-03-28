package com.minetoblend.osugachabot.drops.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface DropRepository : JpaRepository<DropEntity, Long> {
    fun findTopByCreatedByUserIdOrderByCreatedAtDesc(createdByUserId: Long): DropEntity?
}
