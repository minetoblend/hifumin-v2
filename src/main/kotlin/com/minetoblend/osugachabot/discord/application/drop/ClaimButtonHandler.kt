package com.minetoblend.osugachabot.discord.application.drop

import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.drops.ClaimResult
import com.minetoblend.osugachabot.drops.DropService
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import kotlin.time.Clock
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
                val epochSeconds = (Clock.System.now() + result.remaining).epochSeconds
                interaction.respondEphemeral {
                    content = "You are claiming too fast! Try again <t:$epochSeconds:R>."
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
