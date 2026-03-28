package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.drops.ClaimResult
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.users.UserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import org.springframework.stereotype.Component

@Component
class ClaimButtonHandler(private val dropService: DropService) : ButtonInteractionHandler {
    override fun canHandle(customId: String) = ClaimButtonId.isValid(customId)

    override suspend fun ButtonInteractionCreateEvent.handle() {
        val buttonId = ClaimButtonId.fromString(interaction.componentId) ?: return

        val dropId = buttonId.dropId
        val cardIndex = buttonId.cardIndex
        val userId = UserId(interaction.user.id.value.toLong())

        when (val result = dropService.claimCard(dropId, cardIndex, userId)) {
            is ClaimResult.Claimed -> {
                interaction.respondPublic {

                    content = buildString {
                        append("${interaction.user.mention} you claimed the *${result.replica.card.username}* card `${result.replica.id.toDisplayId()}`!")
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

            ClaimResult.Expired -> {
                interaction.respondEphemeral {
                    content = "This drop has expired."
                }
            }
        }
    }
}
