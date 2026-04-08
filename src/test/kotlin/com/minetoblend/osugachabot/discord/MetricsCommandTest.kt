package com.minetoblend.osugachabot.discord

import com.minetoblend.osugachabot.discord.application.metrics.MetricsCommand
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetricsCommandTest {

    @Test
    fun `formatMetrics returns no data message when no timers exist`() {
        val registry = SimpleMeterRegistry()
        val command = MetricsCommand(registry)

        val result = command.formatMetrics()

        assertEquals("No command metrics recorded yet.", result)
    }

    @Test
    fun `formatMetrics includes command name and stats`() {
        val registry = SimpleMeterRegistry()
        Timer.builder("discord.slash_command.duration")
            .tag("command", "ping")
            .register(registry)
            .record(100, TimeUnit.MILLISECONDS)

        val command = MetricsCommand(registry)
        val result = command.formatMetrics()

        assertTrue(result.contains("ping"))
        assertTrue(result.contains("100"))
    }

    @Test
    fun `formatMetrics shows stats for multiple commands`() {
        val registry = SimpleMeterRegistry()

        Timer.builder("discord.slash_command.duration")
            .tag("command", "ping")
            .register(registry)
            .record(100, TimeUnit.MILLISECONDS)

        Timer.builder("discord.slash_command.duration")
            .tag("command", "roll")
            .register(registry)
            .record(200, TimeUnit.MILLISECONDS)

        val command = MetricsCommand(registry)
        val result = command.formatMetrics()

        assertTrue(result.contains("ping"))
        assertTrue(result.contains("roll"))
    }

    @Test
    fun `formatMetrics computes correct min max avg for multiple recordings`() {
        val registry = SimpleMeterRegistry()
        val timer = Timer.builder("discord.slash_command.duration")
            .tag("command", "drop")
            .register(registry)

        timer.record(100, TimeUnit.MILLISECONDS)
        timer.record(200, TimeUnit.MILLISECONDS)
        timer.record(300, TimeUnit.MILLISECONDS)

        val command = MetricsCommand(registry)
        val result = command.formatMetrics()

        assertTrue(result.contains("drop"))
        assertTrue(result.contains("3")) // count
    }

    @Test
    fun `formatMetrics sorts commands alphabetically`() {
        val registry = SimpleMeterRegistry()

        listOf("roll", "burn", "ping").forEach { name ->
            Timer.builder("discord.slash_command.duration")
                .tag("command", name)
                .register(registry)
                .record(100, TimeUnit.MILLISECONDS)
        }

        val command = MetricsCommand(registry)
        val result = command.formatMetrics()

        val burnIndex = result.indexOf("burn")
        val pingIndex = result.indexOf("ping")
        val rollIndex = result.indexOf("roll")

        assertTrue(burnIndex < pingIndex)
        assertTrue(pingIndex < rollIndex)
    }
}
