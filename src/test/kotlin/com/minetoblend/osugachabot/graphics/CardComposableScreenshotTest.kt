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
import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.model.ImageComparisonState
import org.jetbrains.skia.EncodedImageFormat
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
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

        val expectedImage = ImageIO.read(goldenFile)
        val actualImage = ImageIO.read(ByteArrayInputStream(actual))

        val result = ImageComparison(expectedImage, actualImage).apply {
            setPixelToleranceLevel(0.04)
            setAllowingPercentOfDifferentPixels(1.0)
        }.compareImages()

        if (result.imageComparisonState != ImageComparisonState.MATCH) {
            actualDir.mkdirs()
            val actualFile = File(actualDir, "$name.png")
            actualFile.writeBytes(actual)
            throw AssertionError(
                "Screenshot mismatch for '$name'\n" +
                "  actual:   ${actualFile.path}\n" +
                "  expected: ${goldenFile.path}\n" +
                "To update the golden, delete the expected file and re-run."
            )
        }
    }
}
