package com.minetoblend.osugachabot.discord

import com.minetoblend.osugachabot.discord.application.SlashCommandDispatcher
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SlashCommandDispatcherTest {

    private fun buildSdk(exporter: InMemorySpanExporter): OpenTelemetrySdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                    .build()
            )
            .build()

    @Test
    fun `dispatch records timer metric with command tag`() {
        val registry = SimpleMeterRegistry()
        val dispatcher = SlashCommandDispatcher(registry, OpenTelemetrySdk.builder().build())

        runBlocking { dispatcher.dispatch("ping") {} }

        val timer = registry.find("discord.slash_command.duration").tag("command", "ping").timer()
        assertNotNull(timer)
        assertEquals(1, timer.count())
    }

    @Test
    fun `dispatch creates span with command name attribute`() {
        val exporter = InMemorySpanExporter.create()
        val dispatcher = SlashCommandDispatcher(SimpleMeterRegistry(), buildSdk(exporter))

        runBlocking { dispatcher.dispatch("ping") {} }

        val spans = exporter.finishedSpanItems
        assertEquals(1, spans.size)
        assertEquals("discord.slash_command /ping", spans[0].name)
        assertEquals("ping", spans[0].attributes[AttributeKey.stringKey("discord.command.name")])
    }

    @Test
    fun `dispatch records metric for each invocation`() {
        val registry = SimpleMeterRegistry()
        val dispatcher = SlashCommandDispatcher(registry, OpenTelemetrySdk.builder().build())

        runBlocking {
            dispatcher.dispatch("ping") {}
            dispatcher.dispatch("ping") {}
            dispatcher.dispatch("ping") {}
        }

        val timer = registry.find("discord.slash_command.duration").tag("command", "ping").timer()
        assertNotNull(timer)
        assertEquals(3, timer!!.count())
    }

    @Test
    fun `dispatch records separate metrics per command`() {
        val registry = SimpleMeterRegistry()
        val dispatcher = SlashCommandDispatcher(registry, OpenTelemetrySdk.builder().build())

        runBlocking {
            dispatcher.dispatch("ping") {}
            dispatcher.dispatch("roll") {}
        }

        assertEquals(1, registry.find("discord.slash_command.duration").tag("command", "ping").timer()!!.count())
        assertEquals(1, registry.find("discord.slash_command.duration").tag("command", "roll").timer()!!.count())
    }

    @Test
    fun `dispatch rethrows exception and still ends span`() {
        val exporter = InMemorySpanExporter.create()
        val dispatcher = SlashCommandDispatcher(SimpleMeterRegistry(), buildSdk(exporter))

        assertFailsWith<RuntimeException> {
            runBlocking { dispatcher.dispatch("ping") { throw RuntimeException("boom") } }
        }

        assertEquals(1, exporter.finishedSpanItems.size)
    }

    @Test
    fun `dispatch propagates OTel context so child spans are correctly parented`() {
        val exporter = InMemorySpanExporter.create()
        val sdk = buildSdk(exporter)
        val dispatcher = SlashCommandDispatcher(SimpleMeterRegistry(), sdk)
        val innerTracer = sdk.getTracer("test")

        runBlocking {
            dispatcher.dispatch("ping") {
                innerTracer.spanBuilder("child-operation").startSpan().end()
            }
        }

        val spans = exporter.finishedSpanItems
        assertEquals(2, spans.size)
        val parent = spans.first { it.name == "discord.slash_command /ping" }
        val child = spans.first { it.name == "child-operation" }
        assertEquals(parent.spanContext.spanId, child.parentSpanContext.spanId)
    }
}
