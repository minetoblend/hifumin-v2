package com.minetoblend.osugachabot.cooldown.application

import com.minetoblend.osugachabot.cooldown.CooldownResult
import com.minetoblend.osugachabot.cooldown.CooldownService
import com.minetoblend.osugachabot.cooldown.CooldownType
import com.minetoblend.osugachabot.cooldown.persistence.CooldownEntity
import com.minetoblend.osugachabot.cooldown.persistence.CooldownRepository
import com.minetoblend.osugachabot.statuseffect.StatusEffect
import com.minetoblend.osugachabot.statuseffect.StatusEffectService
import com.minetoblend.osugachabot.users.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Service
class CooldownServiceImpl(
    private val cooldownRepository: CooldownRepository,
    private val statusEffectService: StatusEffectService,
) : CooldownService {

    override fun durationFor(type: CooldownType): Duration = when (type) {
        CooldownType.DROP -> 10.minutes
        CooldownType.CLAIM -> 1.minutes
    }

    override fun durationFor(type: CooldownType, userId: UserId): Duration {
        val base = durationFor(type)
        return when (type) {
            CooldownType.DROP ->
                if (statusEffectService.isActive(userId, StatusEffect.DropCooldownReduction)) base * 0.5 else base
            else -> base
        }
    }

    @Transactional
    override fun checkCooldown(userId: UserId, type: CooldownType): CooldownResult {
        val entity = cooldownRepository.findByUserIdAndType(userId.value, type.value)
            ?: return CooldownResult.Ready

        val elapsed = Clock.System.now() - entity.lastUsedAt.toKotlinInstant()
        val remaining = durationFor(type, userId) - elapsed
        return if (remaining.isPositive()) CooldownResult.OnCooldown(remaining) else CooldownResult.Ready
    }

    @Transactional
    override fun recordCooldown(userId: UserId, type: CooldownType) {
        val now = Clock.System.now().toJavaInstant()
        val entity = cooldownRepository.findByUserIdAndType(userId.value, type.value)
        if (entity != null) {
            entity.lastUsedAt = now
        } else {
            cooldownRepository.save(CooldownEntity(userId = userId.value, type = type.value, lastUsedAt = now))
        }
    }

    @Transactional
    override fun tryConsume(userId: UserId, type: CooldownType): CooldownResult {
        val now = Clock.System.now()
        val entity = cooldownRepository.findByUserIdAndType(userId.value, type.value)

        if (entity != null) {
            val remaining = durationFor(type, userId) - (now - entity.lastUsedAt.toKotlinInstant())
            if (remaining.isPositive()) return CooldownResult.OnCooldown(remaining)
            entity.lastUsedAt = now.toJavaInstant()
        } else {
            cooldownRepository.save(CooldownEntity(userId = userId.value, type = type.value, lastUsedAt = now.toJavaInstant()))
        }

        return CooldownResult.Ready
    }
}
