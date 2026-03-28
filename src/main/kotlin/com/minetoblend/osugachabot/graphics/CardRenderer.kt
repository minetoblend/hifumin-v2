package com.minetoblend.osugachabot.graphics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.dp
import com.minetoblend.osugachabot.cards.Card
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import kotlinx.coroutines.CoroutineScope
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

    suspend fun renderCard(card: Card): ByteArray {
        val avatar = loadAvatar(card.userId)

        val image = renderComposeScene(width = CARD_WIDTH + PADDING * 2, height = CARD_HEIGHT + PADDING * 2) {
            CardComposable(
                card = card,
                avatar = avatar,
                modifier = Modifier.padding(PADDING.dp)
            )
        }

        return image
            .encodeToData(EncodedImageFormat.PNG)!!
            .bytes
    }

    suspend fun renderCards(cards: List<Card>): ByteArray {
        val width = (cards.size * (CARD_WIDTH + SPACING)) - SPACING + PADDING * 2
        val height = CARD_HEIGHT + PADDING * 2

        val cardsWithAvatars = coroutineScope {
            cards.map { card ->
                async(Dispatchers.IO) {
                    card to loadAvatar(card.userId)
                }
            }.awaitAll()
        }


        val image = renderComposeScene(width = width, height = height) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SPACING.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(PADDING.dp)
            ) {
                for ((card, avatar) in cardsWithAvatars) {
                    CardComposable(card, avatar)
                }
            }
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