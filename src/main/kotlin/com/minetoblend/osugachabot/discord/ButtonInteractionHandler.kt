package com.minetoblend.osugachabot.discord

import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

interface ButtonInteractionHandler {
    fun canHandle(customId: String): Boolean

    suspend fun ButtonInteractionCreateEvent.handle()
}
