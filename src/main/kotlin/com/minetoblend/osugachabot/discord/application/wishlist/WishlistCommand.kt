package com.minetoblend.osugachabot.discord.application.wishlist

import com.minetoblend.osugachabot.cards.CardService
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.users.toUserId
import com.minetoblend.osugachabot.wishlist.AddToWishlistResult
import com.minetoblend.osugachabot.wishlist.RemoveFromWishlistResult
import com.minetoblend.osugachabot.wishlist.WishlistService
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.builder.message.embed
import org.springframework.stereotype.Component

@Component
class WishlistCommand(
    private val wishlistService: WishlistService,
    private val cardService: CardService,
) : SlashCommand {
    override val name = "wishlist"
    override val description = "Manage your card wishlist"

    override fun ChatInputCreateBuilder.declare() {
        subCommand("add", "Add a card to your wishlist") {
            string("player", "The osu! player username") {
                required = true
            }
        }
        subCommand("remove", "Remove a card from your wishlist") {
            string("player", "The osu! player username") {
                required = true
            }
        }
        subCommand("view", "View your wishlist") {}
    }

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val userId = interaction.user.id.toUserId()

        when (val cmd = interaction.command) {
            is SubCommand -> when (cmd.name) {
                "add" -> {
                    val username = cmd.strings["player"]!!
                    val card = cardService.findByUsername(username)
                    if (card == null) {
                        interaction.respondEphemeral { content = "No card found for player **$username**." }
                        return
                    }
                    when (wishlistService.addToWishlist(userId, card.id)) {
                        AddToWishlistResult.Added ->
                            interaction.respondEphemeral { content = "Added **${card.username}** to your wishlist." }

                        AddToWishlistResult.AlreadyWishlisted ->
                            interaction.respondEphemeral { content = "**${card.username}** is already on your wishlist." }

                        AddToWishlistResult.WishlistFull ->
                            interaction.respondEphemeral {
                                content = "Your wishlist is full (${WishlistService.MAX_WISHLIST_SIZE} cards max). Remove one first."
                            }

                        AddToWishlistResult.CardNotFound ->
                            interaction.respondEphemeral { content = "No card found for player **$username**." }
                    }
                }

                "remove" -> {
                    val username = cmd.strings["player"]!!
                    val card = cardService.findByUsername(username)
                    if (card == null) {
                        interaction.respondEphemeral { content = "No card found for player **$username**." }
                        return
                    }
                    when (wishlistService.removeFromWishlist(userId, card.id)) {
                        RemoveFromWishlistResult.Removed ->
                            interaction.respondEphemeral { content = "Removed **${card.username}** from your wishlist." }

                        RemoveFromWishlistResult.NotWishlisted ->
                            interaction.respondEphemeral { content = "**${card.username}** is not on your wishlist." }
                    }
                }

                "view" -> {
                    val entries = wishlistService.getWishlist(userId)
                    interaction.respondEphemeral {
                        embed {
                            title = "Your Wishlist"
                            description = if (entries.isEmpty()) {
                                "Your wishlist is empty. Use `/wishlist add <player>` to add cards."
                            } else {
                                entries.joinToString("\n") { entry ->
                                    val card = cardService.findById(entry.cardId)
                                    if (card != null) "· **${card.username}** (${card.rarity})"
                                    else "· *(unknown card)*"
                                }
                            }
                            footer {
                                text = "${entries.size} / ${WishlistService.MAX_WISHLIST_SIZE} slots used"
                            }
                        }
                    }
                }
            }

            else -> {}
        }
    }
}
