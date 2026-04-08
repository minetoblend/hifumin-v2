package com.minetoblend.osugachabot.discord.application.daily

import com.minetoblend.osugachabot.cooldown.CooldownResult
import com.minetoblend.osugachabot.cooldown.CooldownService
import com.minetoblend.osugachabot.cooldown.CooldownType
import com.minetoblend.osugachabot.daily.DailyStreakService
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.utils.toDiscordRelativeTimestamp
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.users.toUserId
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

private const val BASE_DAILY_GOLD = 100L
private const val GOLD_PER_STREAK_DAY = 20L
private const val MAX_STREAK_BONUS_DAY = 7

private fun goldRewardForStreak(streak: Int): Long =
    BASE_DAILY_GOLD + (minOf(streak, MAX_STREAK_BONUS_DAY) - 1) * GOLD_PER_STREAK_DAY

@Order(2)
@Component
class DailyCommand(
    private val cooldownService: CooldownService,
    private val inventoryService: InventoryService,
    private val dailyStreakService: DailyStreakService,
) : SlashCommand {
    override val name = "daily"
    override val description = "Claim your daily gold reward"

    override suspend fun ChatInputCommandInteractionCreateEvent.handle() {
        val userId = interaction.user.id.toUserId()

        when (val result = cooldownService.tryConsume(userId, CooldownType.DAILY)) {
            is CooldownResult.Ready -> {
                val streak = dailyStreakService.recordClaim(userId)
                val gold = goldRewardForStreak(streak.currentStreak)

                inventoryService.addItems(userId, ItemType.Gold, gold)

                val streakMessage = if (streak.currentStreak > 1) {
                    " :fire: **${streak.currentStreak} day streak!**"
                } else {
                    ""
                }

                interaction.respondPublic {
                    content = "${interaction.user.mention} you claimed your daily reward: :money_bag: **$gold gold**!$streakMessage"
                }
            }
            is CooldownResult.OnCooldown -> {
                interaction.respondEphemeral {
                    content = "You already claimed your daily reward. Come back ${result.remaining.toDiscordRelativeTimestamp()}!"
                }
            }
        }
    }
}
