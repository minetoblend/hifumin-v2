package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.cards.burnValue
import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.discord.application.burn.BurnButtonId
import com.minetoblend.osugachabot.discord.interactionButtonAccessory
import com.minetoblend.osugachabot.discord.utils.toDiscordRelativeTimestamp
import com.minetoblend.osugachabot.drops.ClaimResult
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.MessageFlag
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.component.section
import dev.kord.rest.builder.message.messageFlags
import org.springframework.stereotype.Component

@Component
class ClaimButtonHandler(private val dropService: DropService) : ButtonInteractionHandler {
    override fun canHandle(customId: String) = ClaimButtonId.isValid(customId)

    override suspend fun ButtonInteractionCreateEvent.handle() {
        val (dropId, cardIndex) = ClaimButtonId.fromString(interaction.componentId) ?: return
        val userId = interaction.user.id.toUserId()

        when (val result = dropService.claimCard(dropId, cardIndex, userId)) {
            is ClaimResult.Claimed -> {
                interaction.respondPublic {
                    messageFlags { +MessageFlag.IsComponentsV2 }
                    section {
                        textDisplay {
                            content = buildString {
                                append("${interaction.user.mention} you claimed the *${result.replica.card.username}* card `${result.replica.id.toDisplayId()}`!")
                                if (result.replica.foil) append(" ✨ It's a foil card!")
                                append(" ")
                                append(
                                    when (result.replica.condition) {
                                        Mint -> "It is in mint condition!"
                                        Good -> "It is in good condition."
                                        Poor -> "It is in poor condition."
                                        Damaged -> "Unfortunately, it is badly damaged."
                                    }
                                )
                            }
                        }
                        interactionButtonAccessory(ButtonStyle.Danger, BurnButtonId(result.replica.id)) {
                            label = "Burn (${result.replica.burnValue} gold)"
                        }
                    }
                }
            }

            is ClaimResult.StolenBack -> {
                interaction.respondPublic {
                    messageFlags { +MessageFlag.IsComponentsV2 }
                    section {
                        textDisplay {
                            content = "${interaction.user.mention} you fought <@${result.stolenFrom.value}> over the *${result.replica.card.username}* card and came out on top!"
                        }
                        interactionButtonAccessory(ButtonStyle.Danger, BurnButtonId(result.replica.id)) {
                            label = "Burn"
                        }
                    }
                }
            }

            is ClaimResult.StealFailed -> {
                interaction.respondPublic {
                    content = "${interaction.user.mention} you fought <@${result.claimedBy.value}> over this card and lost!"
                }
            }

            ClaimResult.StealNotPossible -> {
                interaction.respondEphemeral {
                    content = "This card has already been traded or burned — there's nothing left to steal."
                }
            }

            is ClaimResult.AlreadyClaimed -> {
                interaction.respondPublic {
                    content = "${interaction.user.mention} This card is already claimed!"
                }
            }

            ClaimResult.DropNotFound -> {
                interaction.respondEphemeral {
                    content = "This drop could not be found."
                }
            }

            is ClaimResult.OnCooldown -> {
                interaction.respondEphemeral {
                    content = "You are claiming too fast! Try again ${result.remaining.toDiscordRelativeTimestamp()}."
                }
            }

            ClaimResult.Expired -> {
                interaction.respondEphemeral {
                    content = "This drop has expired."
                }
            }
        }
    }
}
