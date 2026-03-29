package com.minetoblend.osugachabot.discord

import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.users.UserId
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

interface ConsumableItemHandler {
    val itemType: ItemType

    suspend fun ChatInputCommandInteractionCreateEvent.handle(userId: UserId)
}
