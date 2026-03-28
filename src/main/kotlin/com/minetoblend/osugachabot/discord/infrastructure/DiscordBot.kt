package com.minetoblend.osugachabot.discord.infrastructure

import com.minetoblend.osugachabot.discord.DiscordProperties
import com.minetoblend.osugachabot.discord.SlashCommand
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DiscordBot(
    private val properties: DiscordProperties,
    private val slashCommands: List<SlashCommand>,
) : ApplicationRunner {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun run(args: ApplicationArguments) {
        scope.launch {
            val kord = Kord(properties.token)

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
                commandIndex[interaction.invokedCommandName]?.run {
                    handle()
                }
            }

            kord.login()
        }
    }
}
