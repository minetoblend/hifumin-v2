package com.minetoblend.osugachabot.discord.infrastructure

import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.application.SlashCommandDispatcher
import dev.kord.core.Kord
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "discord", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class DiscordBot(
    private val kord: Kord,
    private val slashCommands: List<SlashCommand>,
    private val buttonHandlers: List<ButtonInteractionHandler>,
    private val dispatcher: SlashCommandDispatcher,
    @Qualifier("discordScope") private val scope: CoroutineScope,
) : ApplicationRunner, DisposableBean {

    override fun destroy() {
        scope.launch { kord.logout() }
    }

    override fun run(args: ApplicationArguments) {
        scope.launch {
            @Suppress("UnusedFlow")
            kord.createGlobalApplicationCommands {
                slashCommands.forEach { cmd ->
                    input(cmd.name, cmd.description) {
                        with(cmd) { declare() }
                    }
                }
            }

            val commandIndex = slashCommands.associateBy { it.name }

            kord.on<ChatInputCommandInteractionCreateEvent> {
                val cmd = commandIndex[interaction.invokedCommandName] ?: return@on

                val guildName = if (interaction is GuildChatInputCommandInteraction)
                    (interaction as GuildChatInputCommandInteraction).getGuildOrNull()?.name
                else
                    null

                dispatcher.dispatch(cmd.name, guildName) {
                    cmd.run {
                        handle()
                    }
                }
            }

            kord.on<ButtonInteractionCreateEvent> {
                val handler = buttonHandlers.find { it.canHandle(interaction.componentId) } ?: return@on
                handler.run { handle() }
            }

            kord.login()
        }
    }
}
