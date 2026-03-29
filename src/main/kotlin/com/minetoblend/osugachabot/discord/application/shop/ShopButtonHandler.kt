package com.minetoblend.osugachabot.discord.application.shop

import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.inventory.prettyName
import com.minetoblend.osugachabot.shop.BuyShopItemResult
import com.minetoblend.osugachabot.shop.ShopItemId
import com.minetoblend.osugachabot.shop.ShopService
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import org.springframework.stereotype.Component

@Component
class ShopButtonHandler(private val shopService: ShopService) : ButtonInteractionHandler {
    override fun canHandle(customId: String): Boolean = customId.startsWith("shop:buy:")

    override suspend fun ButtonInteractionCreateEvent.handle() {
        val itemId = ShopItemId(interaction.componentId.substringAfter("shop:buy:"))

        when (val result = shopService.buyShopItem(interaction.user.id.toUserId(), itemId, 1)) {
            InsufficientFunds -> interaction.respondPublic {
                content = "${interaction.user.mention} You cannot afford this item!"
            }

            ItemNotFound -> interaction.respondEphemeral {
                content = "Unknown item ${itemId.value}"
            }

            is Success -> interaction.respondPublic {
                content = "${interaction.user.mention} Successfully purchased ${result.item.item.prettyName} for ${result.item.goldPrice} gold"
            }
        }
    }
}