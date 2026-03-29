package com.minetoblend.osugachabot.discord.infrastructure

import com.minetoblend.osugachabot.discord.DiscordProperties
import dev.kord.core.Kord
import io.ktor.client.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DiscordProperties::class)
class DiscordConfiguration {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Bean
    @Qualifier("discordScope")
    fun discordScope(): CoroutineScope = scope

    @Bean
    @ConditionalOnProperty(prefix = "discord", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun kord(properties: DiscordProperties, openTelemetry: OpenTelemetry): Kord = runBlocking {
        Kord(properties.token) {
            httpClient = HttpClient {
                install(KtorClientTelemetry) {
                    setOpenTelemetry(openTelemetry)
                    attributesExtractor {
                        onStart {
                            attributes.put(AttributeKey.stringKey("peer.service"), "discord")
                        }
                    }
                }
            }
        }
    }

    @PreDestroy
    fun destroy() = scope.cancel()
}
