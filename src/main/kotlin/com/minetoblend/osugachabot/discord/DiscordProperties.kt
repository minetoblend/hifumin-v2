package com.minetoblend.osugachabot.discord

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("discord")
data class DiscordProperties(
    val token: String,
)
