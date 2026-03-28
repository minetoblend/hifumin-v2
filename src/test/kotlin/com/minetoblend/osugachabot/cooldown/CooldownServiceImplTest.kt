package com.minetoblend.osugachabot.cooldown

import com.minetoblend.osugachabot.TestcontainersConfiguration
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

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class CooldownServiceImplTest {

    @Autowired
    private lateinit var cooldownService: CooldownService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val userId = UserId(1L)
    private val otherUserId = UserId(2L)
    private val type = CooldownType.DROP
    private val otherType = CooldownType.CLAIM

    private fun cleanup() {
        jdbcTemplate.update("DELETE FROM cooldowns")
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
}
