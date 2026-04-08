package com.minetoblend.osugachabot.discord.application

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class DiscordMetrics(meterRegistry: MeterRegistry) {

    private val guildCount = AtomicInteger(0)

    init {
        Gauge.builder("discord.guilds", guildCount) { it.get().toDouble() }
            .description("Number of guilds the bot is currently in")
            .register(meterRegistry)
    }

    fun onGuildCreate() = guildCount.incrementAndGet()
    fun onGuildDelete() = guildCount.decrementAndGet()
}
