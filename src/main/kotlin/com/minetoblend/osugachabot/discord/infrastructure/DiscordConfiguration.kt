package com.minetoblend.osugachabot.discord.infrastructure

import com.minetoblend.osugachabot.discord.DiscordProperties
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.springframework.beans.factory.annotation.Qualifier
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

    @PreDestroy
    fun destroy() = scope.cancel()
}
