package com.minetoblend.osugachabot.cooldown.application

import com.minetoblend.osugachabot.cooldown.CooldownResult
import com.minetoblend.osugachabot.cooldown.CooldownService
import com.minetoblend.osugachabot.cooldown.CooldownType
import com.minetoblend.osugachabot.cooldown.persistence.CooldownEntity
import com.minetoblend.osugachabot.cooldown.persistence.CooldownRepository
import com.minetoblend.osugachabot.users.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Service
class CooldownServiceImpl(
    private val cooldownRepository: CooldownRepository,
) : CooldownService {

    @Transactional
    override fun checkCooldown(userId: UserId, type: CooldownType, duration: Duration): CooldownResult {
        val entity = cooldownRepository.findByUserIdAndType(userId.value, type.value)
            ?: return CooldownResult.Ready

        val elapsed = Clock.System.now() - entity.lastUsedAt.toKotlinInstant()
        val remaining = duration - elapsed
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
    override fun tryConsume(userId: UserId, type: CooldownType, duration: Duration): CooldownResult {
        val now = Clock.System.now()
        val entity = cooldownRepository.findByUserIdAndType(userId.value, type.value)

        if (entity != null) {
            val remaining = duration - (now - entity.lastUsedAt.toKotlinInstant())
            if (remaining.isPositive()) return CooldownResult.OnCooldown(remaining)
            entity.lastUsedAt = now.toJavaInstant()
        } else {
            cooldownRepository.save(CooldownEntity(userId = userId.value, type = type.value, lastUsedAt = now.toJavaInstant()))
        }

        return CooldownResult.Ready
    }
}
