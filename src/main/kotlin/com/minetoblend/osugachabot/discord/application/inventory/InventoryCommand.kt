package com.minetoblend.osugachabot.discord.application.inventory

import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.icon
import com.minetoblend.osugachabot.inventory.name
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.effectiveName
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import org.springframework.stereotype.Component

@Component
class InventoryCommand(
    private val inventoryService: InventoryService,
) : SlashCommand {
    override val name = "inventory"
    override val description = "View your inventory"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val userId = interaction.user.id.toUserId()
        val items = inventoryService.getItems(userId)

        interaction.respondPublic {
            embed {
                title = "Inventory for ${interaction.user.effectiveName}"

                description = when {
                    items.all { it.amount == 0L } -> "Your inventory is empty."
                    else -> buildString {
                        for (item in items) {
                            appendLine("${item.icon} ${item.amount} · **${item.name}**")
                        }
                    }
                }
            }
        }
    }
}
