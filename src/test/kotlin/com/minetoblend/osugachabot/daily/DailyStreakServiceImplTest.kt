package com.minetoblend.osugachabot.daily

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class DailyStreakServiceImplTest {

    @Autowired
    private lateinit var dailyStreakService: DailyStreakService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val userId = UserId(100L)
    private val otherUserId = UserId(101L)

    private fun cleanup() {
        jdbcTemplate.update("DELETE FROM daily_streaks WHERE user_id IN (100, 101)")
    }

    private fun setLastClaimedAt(userId: UserId, instant: Instant) {
        jdbcTemplate.update(
            "UPDATE daily_streaks SET last_claimed_at = ? WHERE user_id = ?",
            Timestamp.from(instant),
            userId.value,
        )
    }

    @Test
    fun `first claim starts streak at 1`() {
        cleanup()

        val streak = dailyStreakService.recordClaim(userId)

        assertEquals(1, streak.currentStreak)
    }

    @Test
    fun `claiming again within 48 hours increments streak`() {
        cleanup()

        dailyStreakService.recordClaim(userId)
        setLastClaimedAt(userId, Instant.now().minusSeconds(25 * 3600)) // 25h ago

        val streak = dailyStreakService.recordClaim(userId)

        assertEquals(2, streak.currentStreak)
    }

    @Test
    fun `claiming again after more than 48 hours resets streak to 1`() {
        cleanup()

        dailyStreakService.recordClaim(userId)
        setLastClaimedAt(userId, Instant.now().minusSeconds(49 * 3600)) // 49h ago

        val streak = dailyStreakService.recordClaim(userId)

        assertEquals(1, streak.currentStreak)
    }

    @Test
    fun `streak is independent per user`() {
        cleanup()

        dailyStreakService.recordClaim(userId)
        setLastClaimedAt(userId, Instant.now().minusSeconds(25 * 3600))
        dailyStreakService.recordClaim(userId)

        val otherStreak = dailyStreakService.recordClaim(otherUserId)

        assertEquals(1, otherStreak.currentStreak)
    }

    @Test
    fun `streak increments multiple days in a row`() {
        cleanup()

        dailyStreakService.recordClaim(userId)
        setLastClaimedAt(userId, Instant.now().minusSeconds(25 * 3600))
        dailyStreakService.recordClaim(userId)
        setLastClaimedAt(userId, Instant.now().minusSeconds(25 * 3600))
        val streak = dailyStreakService.recordClaim(userId)

        assertEquals(3, streak.currentStreak)
    }

    @Test
    fun `getStreak returns 0 when user has never claimed`() {
        cleanup()

        val streak = dailyStreakService.getStreak(userId)

        assertEquals(0, streak.currentStreak)
    }

    @Test
    fun `getStreak returns current streak after claims`() {
        cleanup()

        dailyStreakService.recordClaim(userId)
        setLastClaimedAt(userId, Instant.now().minusSeconds(25 * 3600))
        dailyStreakService.recordClaim(userId)

        val streak = dailyStreakService.getStreak(userId)

        assertEquals(2, streak.currentStreak)
    }
}
