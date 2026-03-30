package com.minetoblend.osugachabot.discord.infrastructure

import dev.kord.core.gateway.MasterGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(MasterGateway::class)
class DiscordHealthIndicator(private val gateway: MasterGateway) : HealthIndicator {

    override fun health(): Health {
        val ping = gateway.averagePing
        return if (ping != null) {
            Health.up().withDetail("ping", ping.toString()).build()
        } else {
            Health.down().build()
        }
    }
}
