package com.minetoblend.osugachabot.cooldown.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "cooldowns")
class CooldownEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "type", nullable = false)
    val type: String,

    @Column(name = "last_used_at", nullable = false)
    var lastUsedAt: Instant,
)
