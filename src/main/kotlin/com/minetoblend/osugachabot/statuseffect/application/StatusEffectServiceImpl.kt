package com.minetoblend.osugachabot.statuseffect.application

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
    override fun applyEffect(userId: UserId, effect: StatusEffect, duration: Duration) {
        val now = Clock.System.now()
        val entity = repository.findByUserIdAndEffect(userId.value, effect)
        if (entity != null) {
            val currentExpiry = entity.expiresAt.toKotlinInstant()
            entity.expiresAt = (maxOf(currentExpiry, now) + duration).toJavaInstant()
        } else {
            repository.save(
                ActiveStatusEffectEntity(
                    userId = userId.value,
                    effect = effect,
                    expiresAt = (now + duration).toJavaInstant(),
                )
            )
        }
    }

    override fun isActive(userId: UserId, effect: StatusEffect): Boolean {
        val entity = repository.findByUserIdAndEffect(userId.value, effect) ?: return false
        return entity.expiresAt.isAfter(Clock.System.now().toJavaInstant())
    }
}
