package com.minetoblend.osugachabot.cooldown

import com.minetoblend.osugachabot.users.UserId
import kotlin.time.Duration

enum class CooldownType(val value: String) {
    DROP("drop"),
    CLAIM("claim"),
    DAILY("daily"),
}

interface CooldownService {
    fun durationFor(type: CooldownType): Duration
    fun durationFor(type: CooldownType, userId: UserId): Duration = durationFor(type)

    fun checkCooldown(userId: UserId, type: CooldownType): CooldownResult
    fun recordCooldown(userId: UserId, type: CooldownType)

    /**
     * Atomically checks the cooldown and records it if ready.
     * Returns [CooldownResult.Ready] if the cooldown was consumed, or [CooldownResult.OnCooldown] if still active.
     */
    fun tryConsume(userId: UserId, type: CooldownType): CooldownResult
}

sealed class CooldownResult {
    data object Ready : CooldownResult()
    data class OnCooldown(val remaining: Duration) : CooldownResult()
}
