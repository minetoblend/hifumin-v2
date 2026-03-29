package com.minetoblend.osugachabot.discord.application.use

import com.minetoblend.osugachabot.discord.ConsumableItemHandler
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.inventory.RemoveItemsResult
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(ConsumableItemHandler::class)
class UseCommand(
    private val inventoryService: InventoryService,
    private val consumableItemHandlers: List<ConsumableItemHandler>,
) : SlashCommand {
    override val name = "use"
    override val description = "Use a consumable item from your inventory"

    private val handlersByItemType: Map<ItemType, ConsumableItemHandler> =
        consumableItemHandlers.associateBy { it.itemType }

    override fun ChatInputCreateBuilder.declare() {
        string("item", "The item to use") {
            required = true
            consumableItemHandlers.forEach { handler ->
                choice(handler.itemType.name, handler.itemType.name)
            }
        }
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val itemTypeName = interaction.command.strings["item"] ?: return
        val itemType = ItemType.valueOf(itemTypeName)
        val userId = interaction.user.id.toUserId()
        val handler = handlersByItemType[itemType] ?: return

        when (inventoryService.removeItems(userId, itemType, 1)) {
            RemoveItemsResult.InsufficientItems -> interaction.respondEphemeral {
                content = "You don't have any **${itemType.name}** to use!"
            }
            RemoveItemsResult.Success -> with(handler) { handle(userId) }
        }
    }
}
