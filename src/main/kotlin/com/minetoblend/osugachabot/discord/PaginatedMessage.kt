package com.minetoblend.osugachabot.discord

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.response.PublicMessageInteractionResponse
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.component.actionRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.time.Duration

abstract class PaginatedMessage(
    protected val scope: CoroutineScope,
    protected val interaction: ChatInputCommandInteraction,
) {
    open val pageSize: Int = 10
    open val sort: Sort = Sort.unsorted()

    abstract suspend fun getItemCount(): Int

    abstract suspend fun MessageBuilder.renderPage(page: PageRequest)

    var currentPage = 0
    var pageCount = 0
        private set


    private lateinit var response: PublicMessageInteractionResponse

    suspend fun run(duration: Duration) {
        pageCount = (getItemCount() + pageSize - 1) / pageSize

        response = interaction
            .deferPublicResponse()
            .respond {
                render()
            }

        val job = scope.launch {
            interaction.kord.on<ButtonInteractionCreateEvent>(this) {
                if (interaction.message.id != response.message.id)
                    return@on

                if (interaction.user.id != this.interaction.user.id)
                    return@on

                handleButtonResponse()
            }
        }
        delay(duration)
        job.cancel()

        response.edit {
            components = mutableListOf()
        }
    }

    private suspend fun ButtonInteractionCreateEvent.handleButtonResponse() {
        when (interaction.componentId) {
            "page:start" -> {
                currentPage = 0
                interaction.updatePublicMessage {
                    render()
                }
            }

            "page:end" -> {
                currentPage = (pageCount - 1).coerceIn(0 until pageCount)
                interaction.updatePublicMessage {
                    render()
                }
            }

            "page:prev" -> {
                currentPage = (currentPage - 1).coerceIn(0 until pageCount)
                interaction.updatePublicMessage {
                    render()
                }
            }

            "page:next" -> {
                currentPage = (currentPage + 1).coerceIn(0 until pageCount)
                interaction.updatePublicMessage {
                    render()
                }
            }
        }
    }

    private suspend fun MessageBuilder.render() {
        renderPage(PageRequest.of(currentPage, pageSize, sort))

        actionRow {
            interactionButton(ButtonStyle.Primary, "page:start") {
                label = "⏮"
                disabled = currentPage <= 0
            }
            interactionButton(ButtonStyle.Primary, "page:prev") {
                label = "◀"
                disabled = currentPage <= 0
            }
            interactionButton(ButtonStyle.Primary, "page:next") {
                label = "▶"
                disabled = currentPage >= pageCount - 1
            }
            interactionButton(ButtonStyle.Primary, "page:end") {
                label = "⏭"
                disabled = currentPage >= pageCount - 1
            }
        }
    }

    protected fun EmbedBuilder.pageFooter() {
        if (pageCount <= 0)
            return

        footer {
            text = "Page ${currentPage + 1} / $pageCount"
        }
    }
}