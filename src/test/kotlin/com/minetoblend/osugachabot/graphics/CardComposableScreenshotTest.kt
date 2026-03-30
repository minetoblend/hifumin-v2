package com.minetoblend.osugachabot.graphics

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.dp
import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.CardRarity
import com.minetoblend.osugachabot.generated.resources.Res
import com.minetoblend.osugachabot.generated.resources.default_avatar
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import kotlin.math.abs
import kotlin.test.Test

class CardComposableScreenshotTest {

    private val goldenDir = File("src/test/resources/screenshots")
    private val actualDir = File("build/test-screenshots")

    @Test
    fun `card N rarity matches snapshot`() =
        assertMatchesSnapshot("card-N", render(cardForRarity(CardRarity.N)))

    @Test
    fun `card R rarity matches snapshot`() =
        assertMatchesSnapshot("card-R", render(cardForRarity(CardRarity.R)))

    @Test
    fun `card SR rarity matches snapshot`() =
        assertMatchesSnapshot("card-SR", render(cardForRarity(CardRarity.SR)))

    @Test
    fun `card SSR rarity matches snapshot`() =
        assertMatchesSnapshot("card-SSR", render(cardForRarity(CardRarity.SSR)))

    @Test
    fun `card EX rarity matches snapshot`() =
        assertMatchesSnapshot("card-EX", render(cardForRarity(CardRarity.EX)))

    @Test
    fun `card N foil matches snapshot`() =
        assertMatchesSnapshot("card-N-foil", render(cardForRarity(CardRarity.N), foil = true))

    @Test
    fun `card R foil matches snapshot`() =
        assertMatchesSnapshot("card-R-foil", render(cardForRarity(CardRarity.R), foil = true))

    @Test
    fun `card SR foil matches snapshot`() =
        assertMatchesSnapshot("card-SR-foil", render(cardForRarity(CardRarity.SR), foil = true))

    @Test
    fun `card SSR foil matches snapshot`() =
        assertMatchesSnapshot("card-SSR-foil", render(cardForRarity(CardRarity.SSR), foil = true))

    @Test
    fun `card EX foil matches snapshot`() =
        assertMatchesSnapshot("card-EX-foil", render(cardForRarity(CardRarity.EX), foil = true))

    @Test
    fun `card without global rank matches snapshot`() =
        assertMatchesSnapshot("card-no-rank", render(cardForRarity(CardRarity.N, globalRank = null)))

    private fun cardForRarity(rarity: CardRarity, globalRank: Int? = 12345) = Card(
        id = CardId(1),
        username = "TestPlayer",
        countryCode = "US",
        title = null,
        followerCount = 10_000,
        globalRank = globalRank,
        userId = 0,
        rarity = rarity,
    )

    private fun render(card: Card, foil: Boolean = false): ByteArray {
        val padding = 64
        val image = renderComposeScene(
            width = CardRenderer.CARD_WIDTH + padding * 2,
            height = CardRenderer.CARD_HEIGHT + padding * 2,
        ) {
            val avatar = imageResource(Res.drawable.default_avatar)
            CardComposable(
                card = card,
                avatar = avatar,
                foil = foil,
                modifier = Modifier.padding(padding.dp),
            )
        }
        return image.encodeToData(EncodedImageFormat.PNG)!!.bytes
    }

    private fun assertMatchesSnapshot(name: String, actual: ByteArray) {
        goldenDir.mkdirs()
        val goldenFile = File(goldenDir, "$name.png")

        if (!goldenFile.exists()) {
            goldenFile.writeBytes(actual)
            return
        }

        val expectedBitmap = Bitmap.makeFromImage(Image.makeFromEncoded(goldenFile.readBytes()))
        val actualBitmap = Bitmap.makeFromImage(Image.makeFromEncoded(actual))

        if (expectedBitmap.width != actualBitmap.width || expectedBitmap.height != actualBitmap.height) {
            actualDir.mkdirs()
            File(actualDir, "$name.png").writeBytes(actual)
            throw AssertionError(
                "Screenshot size mismatch for '$name': " +
                "expected ${expectedBitmap.width}x${expectedBitmap.height}, " +
                "got ${actualBitmap.width}x${actualBitmap.height}"
            )
        }

        val similarity = bitmapSimilarity(expectedBitmap, actualBitmap)

        if (similarity.maxChannelDiff > 10 || similarity.diffRatio > 0.01) {
            actualDir.mkdirs()
            val actualFile = File(actualDir, "$name.png")
            actualFile.writeBytes(actual)
            throw AssertionError(
                "Screenshot mismatch for '$name' " +
                "(maxDiff=${similarity.maxChannelDiff}, diffPixels=${similarity.diffPixels}/${similarity.totalPixels} = ${"%.2f".format(similarity.diffRatio * 100)}%)\n" +
                "  actual:   ${actualFile.path}\n" +
                "  expected: ${goldenFile.path}\n" +
                "To update the golden, delete the expected file and re-run."
            )
        }
    }

    private data class BitmapSimilarity(val maxChannelDiff: Int, val diffPixels: Int, val totalPixels: Int) {
        val diffRatio: Double get() = diffPixels.toDouble() / totalPixels
    }

    private fun bitmapSimilarity(expected: Bitmap, actual: Bitmap): BitmapSimilarity {
        var maxDiff = 0
        var diffPixels = 0
        for (y in 0 until expected.height) {
            for (x in 0 until expected.width) {
                val exp = expected.getColor(x, y)
                val act = actual.getColor(x, y)
                val diff = maxOf(
                    abs((exp shr 16 and 0xFF) - (act shr 16 and 0xFF)),
                    abs((exp shr 8 and 0xFF) - (act shr 8 and 0xFF)),
                    abs((exp and 0xFF) - (act and 0xFF)),
                    abs((exp ushr 24) - (act ushr 24)),
                )
                if (diff > 0) diffPixels++
                if (diff > maxDiff) maxDiff = diff
            }
        }
        return BitmapSimilarity(maxDiff, diffPixels, expected.width * expected.height)
    }
}
