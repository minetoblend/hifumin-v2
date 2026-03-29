package com.minetoblend.osugachabot.discord.application.shop

import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.shop.ShopService
import dev.kord.common.entity.MessageFlag
import dev.kord.common.entity.SeparatorSpacingSize
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.component.interactionButtonAccessory
import dev.kord.rest.builder.component.section
import dev.kord.rest.builder.component.separator
import dev.kord.rest.builder.message.container
import dev.kord.rest.builder.message.messageFlags
import org.springframework.stereotype.Component

@Component
class ShopCommand(private val shopService: ShopService) : SlashCommand {
    override val name = "shop"
    override val description = "View the shop"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val items = shopService.getShopItems()

        interaction.respondPublic {
            messageFlags {
                +MessageFlag.IsComponentsV2
            }

            container {
                for ((index, item) in items.withIndex()) {
                    if (index > 0) {
                        separator {
                            spacing = Small
                        }
                    }

                    section {
                        textDisplay {
                            content = "**${item.name}** · `${item.goldPrice} gold`"
                        }
                        if (item.description != null) {
                            textDisplay {
                                content = "*${item.description}*"
                            }
                        }

                        interactionButtonAccessory(Primary, "shop:buy:${item.id.value}") {
                            label = "Buy"
                        }
                    }
                }
            }
        }
    }
}