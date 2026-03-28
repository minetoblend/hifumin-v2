package com.minetoblend.osugachabot.discord.infrastructure

import com.minetoblend.osugachabot.discord.DiscordProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DiscordProperties::class)
class DiscordConfiguration
