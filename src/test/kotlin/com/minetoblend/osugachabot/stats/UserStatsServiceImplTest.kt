package com.minetoblend.osugachabot.stats

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class UserStatsServiceImplTest {

    @Autowired
    private lateinit var userStatsService: UserStatsService

    @Test
    fun `getStats returns zero for all actions when user has no stats`() {
        val stats = userStatsService.getStats(UserId(999L))

        UserAction.entries.forEach { action ->
            assertEquals(0L, stats[action], "Expected 0 for ${action.name}")
        }
    }

    @Test
    fun `getStats includes SUPER_DROP action`() {
        val stats = userStatsService.getStats(UserId(999L))

        assertTrue(stats.containsKey(UserAction.SUPER_DROP), "Stats should include SUPER_DROP")
    }
}
