package com.minetoblend.osugachabot.discord.infrastructure

import com.minetoblend.osugachabot.discord.ButtonInteractionHandler
import com.minetoblend.osugachabot.discord.DiscordProperties
import com.minetoblend.osugachabot.discord.SlashCommand
import com.minetoblend.osugachabot.discord.application.SlashCommandDispatcher
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import io.ktor.client.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DiscordBot(
    private val properties: DiscordProperties,
    private val slashCommands: List<SlashCommand>,
    private val buttonHandlers: List<ButtonInteractionHandler>,
    private val dispatcher: SlashCommandDispatcher,
    private val openTelemetry: OpenTelemetry,
    @Qualifier("discordScope") private val scope: CoroutineScope,
) : ApplicationRunner, DisposableBean {

    private var kord: Kord? = null

    override fun destroy() {
        kord?.let { scope.launch { it.logout() } }
    }

    override fun run(args: ApplicationArguments) {
        scope.launch {
            val kord = Kord(properties.token) {
                httpClient = HttpClient {
                    install(KtorClientTelemetry) {
                        setOpenTelemetry(openTelemetry)
                        attributesExtractor {
                            onStart {
                                attributes.put(AttributeKey.stringKey("peer.service"), "discord")
                            }
                        }
                    }
                }
            }
            this@DiscordBot.kord = kord

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
                dispatcher.dispatch(cmd.name) {
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
