package com.minetoblend.osugachabot.cooldown

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.statuseffect.StatusEffect
import com.minetoblend.osugachabot.statuseffect.StatusEffectService
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class CooldownServiceImplTest {

    @Autowired
    private lateinit var cooldownService: CooldownService

    @Autowired
    private lateinit var statusEffectService: StatusEffectService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val userId = UserId(1L)
    private val otherUserId = UserId(2L)
    private val type = CooldownType.DROP
    private val otherType = CooldownType.CLAIM
    private val dailyType = CooldownType.DAILY

    private fun cleanup() {
        jdbcTemplate.update("DELETE FROM cooldowns")
        jdbcTemplate.update("DELETE FROM active_status_effects")
    }

    @Test
    fun `checkCooldown returns Ready when no cooldown recorded`() {
        cleanup()

        val result = cooldownService.checkCooldown(userId, type)

        assertIs<CooldownResult.Ready>(result)
    }

    @Test
    fun `checkCooldown returns OnCooldown immediately after recordCooldown`() {
        cleanup()

        cooldownService.recordCooldown(userId, type)
        val result = cooldownService.checkCooldown(userId, type)

        assertIs<CooldownResult.OnCooldown>(result)
        assertTrue(result.remaining.isPositive())
    }

    @Test
    fun `checkCooldown returns Ready after duration has elapsed`() {
        cleanup()

        cooldownService.recordCooldown(userId, type)
        jdbcTemplate.update(
            "UPDATE cooldowns SET last_used_at = ? WHERE user_id = ? AND type = ?",
            Timestamp.from(Instant.now().minusSeconds(cooldownService.durationFor(type).inWholeSeconds + 1)),
            userId.value,
            type.value,
        )

        val result = cooldownService.checkCooldown(userId, type)

        assertIs<CooldownResult.Ready>(result)
    }

    @Test
    fun `checkCooldown is independent per user`() {
        cleanup()

        cooldownService.recordCooldown(userId, type)
        val result = cooldownService.checkCooldown(otherUserId, type)

        assertIs<CooldownResult.Ready>(result)
    }

    @Test
    fun `checkCooldown is independent per type`() {
        cleanup()

        cooldownService.recordCooldown(userId, type)
        val result = cooldownService.checkCooldown(userId, otherType)

        assertIs<CooldownResult.Ready>(result)
    }

    @Test
    fun `recordCooldown updates last_used_at on subsequent calls`() {
        cleanup()

        cooldownService.recordCooldown(userId, type)
        jdbcTemplate.update(
            "UPDATE cooldowns SET last_used_at = ? WHERE user_id = ? AND type = ?",
            Timestamp.from(Instant.now().minusSeconds(cooldownService.durationFor(type).inWholeSeconds + 1)),
            userId.value,
            type.value,
        )
        cooldownService.recordCooldown(userId, type)

        val result = cooldownService.checkCooldown(userId, type)

        assertIs<CooldownResult.OnCooldown>(result)
    }

    @Test
    fun `tryConsume returns Ready and records cooldown on first call`() {
        cleanup()

        val result = cooldownService.tryConsume(userId, type)

        assertIs<CooldownResult.Ready>(result)
        assertIs<CooldownResult.OnCooldown>(cooldownService.checkCooldown(userId, type))
    }

    @Test
    fun `tryConsume returns OnCooldown without updating when already active`() {
        cleanup()

        cooldownService.tryConsume(userId, type)
        val result = cooldownService.tryConsume(userId, type)

        assertIs<CooldownResult.OnCooldown>(result)
        assertTrue(result.remaining.isPositive())
    }

    @Test
    fun `tryConsume returns Ready and resets cooldown after duration has elapsed`() {
        cleanup()

        cooldownService.tryConsume(userId, type)
        jdbcTemplate.update(
            "UPDATE cooldowns SET last_used_at = ? WHERE user_id = ? AND type = ?",
            Timestamp.from(Instant.now().minusSeconds(cooldownService.durationFor(type).inWholeSeconds + 1)),
            userId.value,
            type.value,
        )

        val result = cooldownService.tryConsume(userId, type)

        assertIs<CooldownResult.Ready>(result)
        assertIs<CooldownResult.OnCooldown>(cooldownService.checkCooldown(userId, type))
    }

    @Test
    fun `durationFor with userId returns halved duration when DROP_COOLDOWN_REDUCTION is active`() {
        cleanup()

        statusEffectService.applyEffect(userId, StatusEffect.DropCooldownReduction, 6.hours)
        val boostedDuration = cooldownService.durationFor(CooldownType.DROP, userId)
        val baseDuration = cooldownService.durationFor(CooldownType.DROP)

        assertTrue(boostedDuration < baseDuration)
        assertTrue(boostedDuration == baseDuration * 0.5)
    }

    @Test
    fun `durationFor with userId returns base duration when no effect active`() {
        cleanup()

        val duration = cooldownService.durationFor(CooldownType.DROP, userId)

        assertTrue(duration == cooldownService.durationFor(CooldownType.DROP))
    }

    @Test
    fun `DAILY cooldown duration is 24 hours`() {
        val duration = cooldownService.durationFor(dailyType)

        assertTrue(duration == 24.hours)
    }

    @Test
    fun `DAILY tryConsume returns Ready on first use and OnCooldown immediately after`() {
        cleanup()

        val first = cooldownService.tryConsume(userId, dailyType)
        val second = cooldownService.tryConsume(userId, dailyType)

        assertIs<CooldownResult.Ready>(first)
        assertIs<CooldownResult.OnCooldown>(second)
        assertTrue((second as CooldownResult.OnCooldown).remaining.isPositive())
    }

    @Test
    fun `tryConsume respects active status effect when checking cooldown`() {
        cleanup()

        statusEffectService.applyEffect(userId, StatusEffect.DropCooldownReduction, 6.hours)
        cooldownService.tryConsume(userId, type)

        val baseDuration = cooldownService.durationFor(type)
        jdbcTemplate.update(
            "UPDATE cooldowns SET last_used_at = ? WHERE user_id = ? AND type = ?",
            Timestamp.from(Instant.now().minusSeconds(baseDuration.inWholeSeconds / 2 + 1)),
            userId.value,
            type.value,
        )

        val result = cooldownService.tryConsume(userId, type)

        assertIs<CooldownResult.Ready>(result)
    }
}
