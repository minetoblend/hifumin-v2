package com.minetoblend.osugachabot.statuseffect.application

import com.minetoblend.osugachabot.statuseffect.ApplyStatusEffectResult
import com.minetoblend.osugachabot.statuseffect.StatusEffect
import com.minetoblend.osugachabot.statuseffect.StatusEffectService
import com.minetoblend.osugachabot.statuseffect.persistence.ActiveStatusEffectEntity
import com.minetoblend.osugachabot.statuseffect.persistence.ActiveStatusEffectRepository
import com.minetoblend.osugachabot.users.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Service
class StatusEffectServiceImpl(
    private val repository: ActiveStatusEffectRepository,
) : StatusEffectService {

    @Transactional
    override fun applyEffect(userId: UserId, effect: StatusEffect, duration: Duration): ApplyStatusEffectResult {
        val now = Clock.System.now()
        val entity = repository.findByUserIdAndEffect(userId.value, effect)
        return if (entity != null) {
            val currentExpiry = entity.expiresAt.toKotlinInstant()
            val expiresAt = maxOf(currentExpiry, now) + duration

            entity.expiresAt = expiresAt.toJavaInstant()
            repository.save(entity)

            ApplyStatusEffectResult(expiresAt)
        } else {
            val expiresAt = (now + duration)

            repository.save(
                ActiveStatusEffectEntity(
                    userId = userId.value,
                    effect = effect,
                    expiresAt = expiresAt.toJavaInstant(),
                )
            )

            ApplyStatusEffectResult(expiresAt)
        }
    }

    override fun isActive(userId: UserId, effect: StatusEffect): Boolean {
        val entity = repository.findByUserIdAndEffect(userId.value, effect) ?: return false
        return entity.expiresAt.isAfter(Clock.System.now().toJavaInstant())
    }
}
