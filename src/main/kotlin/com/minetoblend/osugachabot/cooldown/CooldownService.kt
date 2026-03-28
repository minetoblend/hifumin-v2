package com.minetoblend.osugachabot.cooldown

import com.minetoblend.osugachabot.users.UserId
import kotlin.time.Duration

@JvmInline
value class CooldownType(val value: String)

interface CooldownService {
    fun checkCooldown(userId: UserId, type: CooldownType, duration: Duration): CooldownResult
    fun recordCooldown(userId: UserId, type: CooldownType)

    /**
     * Atomically checks the cooldown and records it if ready.
     * Returns [CooldownResult.Ready] if the cooldown was consumed, or [CooldownResult.OnCooldown] if still active.
     */
    fun tryConsume(userId: UserId, type: CooldownType, duration: Duration): CooldownResult
}

sealed class CooldownResult {
    data object Ready : CooldownResult()
    data class OnCooldown(val remaining: Duration) : CooldownResult()
}
