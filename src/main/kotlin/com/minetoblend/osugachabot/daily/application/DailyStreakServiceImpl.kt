package com.minetoblend.osugachabot.daily.application

import com.minetoblend.osugachabot.daily.DailyStreak
import com.minetoblend.osugachabot.daily.DailyStreakService
import com.minetoblend.osugachabot.daily.persistence.DailyStreakEntity
import com.minetoblend.osugachabot.daily.persistence.DailyStreakRepository
import com.minetoblend.osugachabot.users.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

private val STREAK_BREAK_WINDOW = 48.hours

@Service
class DailyStreakServiceImpl(
    private val dailyStreakRepository: DailyStreakRepository,
) : DailyStreakService {

    @Transactional
    override fun recordClaim(userId: UserId): DailyStreak {
        val now = Clock.System.now()
        val entity = dailyStreakRepository.findByUserIdForUpdate(userId.value)

        if (entity == null) {
            val newEntity = DailyStreakEntity(
                userId = userId.value,
                currentStreak = 1,
                lastClaimedAt = now.toJavaInstant(),
            )
            dailyStreakRepository.save(newEntity)
            return DailyStreak(userId, 1)
        }

        val elapsed = now - entity.lastClaimedAt.toKotlinInstant()
        entity.currentStreak = if (elapsed <= STREAK_BREAK_WINDOW) entity.currentStreak + 1 else 1
        entity.lastClaimedAt = now.toJavaInstant()

        return DailyStreak(userId, entity.currentStreak)
    }

    @Transactional(readOnly = true)
    override fun getStreak(userId: UserId): DailyStreak {
        val entity = dailyStreakRepository.findByUserId(userId.value)
            ?: return DailyStreak(userId, 0)
        return DailyStreak(userId, entity.currentStreak)
    }
}
