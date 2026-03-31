package com.minetoblend.osugachabot.discord.application

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SlashCommandDispatcher(
    private val meterRegistry: MeterRegistry,
    openTelemetry: OpenTelemetry,
) {
    private val tracer = openTelemetry.getTracer("osugachabot.discord")
    private val logger = LoggerFactory.getLogger(SlashCommandDispatcher::class.java)

    suspend fun dispatch(commandName: String, guildName: String? = null, block: suspend () -> Unit) {
        logger.info("Invoking slash command: /{} (guild: {})", commandName, guildName ?: "unknown")
        val span = tracer.spanBuilder("discord.slash_command /$commandName")
            .setAttribute("discord.command.name", commandName)
            .startSpan()
        val otelContext = span.storeInContext(Context.current())
        val sample = Timer.start(meterRegistry)
        try {
            withContext(otelContext.asContextElement()) {
                block()
            }
            span.setStatus(StatusCode.OK)
            logger.info("Slash command completed: /{} (guild: {})", commandName, guildName ?: "unknown")
        } catch (e: Exception) {
            span.setStatus(StatusCode.ERROR)
            span.recordException(e)
            logger.error("Slash command failed: /{} (guild: {})", commandName, guildName ?: "unknown", e)
            throw e
        } finally {
            sample.stop(
                Timer.builder("discord.slash_command.duration")
                    .tag("command", commandName)
                    .register(meterRegistry)
            )
            span.end()
        }
    }
}
