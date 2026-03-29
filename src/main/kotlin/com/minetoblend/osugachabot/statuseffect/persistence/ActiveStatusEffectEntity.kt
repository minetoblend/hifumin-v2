package com.minetoblend.osugachabot.statuseffect.persistence

import com.minetoblend.osugachabot.statuseffect.StatusEffect
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "active_status_effects",
    indexes = [
        Index(name = "idx_active_status_effects_user_effect", columnList = "user_id, effect", unique = true),
    ]
)
class ActiveStatusEffectEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false)
    val effect: StatusEffect,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
)
