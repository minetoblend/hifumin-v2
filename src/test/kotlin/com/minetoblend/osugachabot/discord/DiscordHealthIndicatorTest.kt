package com.minetoblend.osugachabot.discord

import com.minetoblend.osugachabot.discord.infrastructure.DiscordHealthIndicator
import dev.kord.core.gateway.MasterGateway
import dev.kord.core.gateway.ShardEvent
import dev.kord.gateway.Gateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.boot.health.contributor.Status
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DiscordHealthIndicatorTest {

    private fun gateway(ping: Duration?) = object : MasterGateway {
        override val gateways: Map<Int, Gateway> = emptyMap()
        override val averagePing: Duration? = ping
        override val events: Flow<ShardEvent> = emptyFlow()
    }

    @Test
    fun `health returns UP with ping detail when gateway has average ping`() {
        val indicator = DiscordHealthIndicator(gateway(42.milliseconds))

        val health = indicator.health()

        assertEquals(Status.UP, health.status)
        assertEquals("42ms", health.details["ping"])
    }

    @Test
    fun `health returns DOWN when gateway has no average ping`() {
        val indicator = DiscordHealthIndicator(gateway(null))

        val health = indicator.health()

        assertEquals(Status.DOWN, health.status)
    }
}
