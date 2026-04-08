package com.minetoblend.osugachabot.graphics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.dp
import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.tournament.TournamentBracket
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.skia.EncodedImageFormat
import org.springframework.stereotype.Component

@Component
class CardRenderer(
    private val openTelemetry: OpenTelemetry,
) {

    suspend fun renderCard(card: Card, foil: Boolean = false): ByteArray {
        val avatar = loadAvatar(card.userId)

        val image = renderComposeScene(width = CARD_WIDTH + PADDING * 2, height = CARD_HEIGHT + PADDING * 2) {
            CardComposable(
                card = card,
                avatar = avatar,
                foil = foil,
                modifier = Modifier.padding(PADDING.dp)
            )
        }

        return image
            .encodeToData(EncodedImageFormat.PNG)!!
            .bytes
    }

    suspend fun renderCards(cards: List<RenderableCard>): ByteArray {
        val columns = cards.size.coerceAtMost(5)
        val rows = (cards.size + 4) / 5

        val width = (columns * (CARD_WIDTH + SPACING)) - SPACING + PADDING * 2
        val height = rows * (CARD_HEIGHT + SPACING) - SPACING + PADDING * 2

        val cardsWithAvatars = coroutineScope {
            cards.map { renderable ->
                async(Dispatchers.IO) {
                    renderable to loadAvatar(renderable.card.userId)
                }
            }.awaitAll()
        }

        val image = renderComposeScene(width = width, height = height) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SPACING.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(SPACING.dp, Alignment.CenterVertically),
                modifier = Modifier.padding(PADDING.dp)
            ) {
                for ((renderable, avatar) in cardsWithAvatars) {
                    CardComposable(renderable.card, avatar, renderable.foil)
                }
            }
        }

        return image
            .encodeToData(EncodedImageFormat.PNG)!!
            .bytes
    }

    suspend fun renderBracket(bracket: TournamentBracket, tournamentName: String): ByteArray {
        // Collect all unique osu user IDs from the bracket entries
        val userIds = bracket.rounds
            .flatMap { it.matches }
            .flatMap { listOfNotNull(it.entry1, it.entry2) }
            .map { it.cardReplica.card.userId }
            .distinct()

        val avatars: Map<Long, ImageBitmap?> = coroutineScope {
            userIds.associateWith { userId ->
                async(Dispatchers.IO) { loadAvatar(userId) }
            }.mapValues { it.value.await() }
        }

        val (width, height) = computeBracketSize(bracket)

        val image = renderComposeScene(width = width, height = height) {
            BracketComposable(
                bracket = bracket,
                tournamentName = tournamentName,
                avatars = avatars,
            )
        }

        return image
            .encodeToData(EncodedImageFormat.PNG)!!
            .bytes
    }

    private val client = HttpClient {
        install(KtorClientTelemetry) {
            setOpenTelemetry(openTelemetry)
            attributesExtractor {
                onStart {
                    attributes.put(AttributeKey.stringKey("peer.service"), "a.ppy.sh")
                }
            }
        }
    }

    private suspend fun loadAvatar(id: Long): ImageBitmap? {
        val url = "https://a.ppy.sh/$id"

        return runCatching {
            client.get(url)
                .takeIf { it.status.isSuccess() }
                ?.bodyAsBytes()
                ?.decodeToImageBitmap()
        }.getOrNull()

    }

    companion object {
        const val CARD_WIDTH = 256
        const val CARD_HEIGHT = 384

        const val PADDING = 2
        const val SPACING = 32
    }
}