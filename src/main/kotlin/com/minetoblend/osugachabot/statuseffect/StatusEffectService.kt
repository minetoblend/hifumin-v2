package com.minetoblend.osugachabot.statuseffect

import com.minetoblend.osugachabot.users.UserId
import kotlin.time.Duration
import kotlin.time.Instant

interface StatusEffectService {
    fun applyEffect(userId: UserId, effect: StatusEffect, duration: Duration): ApplyStatusEffectResult
    fun isActive(userId: UserId, effect: StatusEffect): Boolean
}

data class ApplyStatusEffectResult(val expiresAt: Instant)