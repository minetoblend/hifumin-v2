package com.minetoblend.osugachabot.discord

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

interface SlashCommand {
    val name: String
    val description: String

    fun ChatInputCreateBuilder.declare() {}

    suspend fun ChatInputCommandInteractionCreateEvent.handle()
}
