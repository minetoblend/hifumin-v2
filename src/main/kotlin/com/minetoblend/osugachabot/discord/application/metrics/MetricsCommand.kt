package com.minetoblend.osugachabot.discord.application.metrics

import com.minetoblend.osugachabot.discord.SlashCommand
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit
import org.springframework.stereotype.Component

@Component
class MetricsCommand(
    private val meterRegistry: MeterRegistry,
) : SlashCommand {
    override val name = "metrics"
    override val description = "Show min/max/avg duration of each slash command"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        interaction.respondEphemeral {
            embed {
                title = "Command Metrics"
                description = formatMetrics()
            }
        }
    }

    fun formatMetrics(): String {
        val timers = meterRegistry.find("discord.slash_command.duration").timers()

        if (timers.isEmpty()) {
            return "No command metrics recorded yet."
        }

        return timers
            .sortedBy { it.id.getTag("command") }
            .joinToString("\n") { timer ->
                val command = timer.id.getTag("command")
                val count = timer.count()
                val max = formatMs(timer.max(TimeUnit.MILLISECONDS))
                val avg = formatMs(timer.mean(TimeUnit.MILLISECONDS))
                "**/$command** — ${count}x | avg: $avg | max: $max"
            }
    }

    private fun formatMs(ms: Double): String =
        if (ms < 1000) "%.0fms".format(ms)
        else "%.1fs".format(ms / 1000)
}
