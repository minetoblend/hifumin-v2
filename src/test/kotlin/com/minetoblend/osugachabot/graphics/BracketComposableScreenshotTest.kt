package com.minetoblend.osugachabot.graphics

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.dp
import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.model.ImageComparisonState
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardRarity
import com.minetoblend.osugachabot.tournament.*
import org.jetbrains.skia.EncodedImageFormat
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class BracketComposableScreenshotTest {

    private val goldenDir = File("src/test/resources/screenshots")
    private val actualDir = File("build/test-screenshots")

    private fun entry(
        userId: Long,
        username: String,
        weight: Double,
        rarity: CardRarity = CardRarity.SR,
        condition: CardCondition = CardCondition.Mint,
        foil: Boolean = false,
    ) = TournamentMatchEntry(
        userId = userId,
        cardReplica = SnapshotCardReplica(
            id = userId,
            card = SnapshotCard(
                id = userId,
                userId = userId,
                username = username,
                countryCode = "US",
                title = null,
                followerCount = weight.toInt(),
                globalRank = (10000 - weight).toInt().coerceAtLeast(1),
                rarity = rarity,
            ),
            condition = condition,
            foil = foil,
        ),
        weight = weight,
    )

    private fun render(bracket: TournamentBracket, name: String): ByteArray {
        val size = computeBracketSize(bracket)
        val image = renderComposeScene(width = size.width, height = size.height) {
            BracketComposable(
                bracket = bracket,
                tournamentName = name,
                avatars = emptyMap(),
                modifier = Modifier.size(size.width.dp, size.height.dp),
            )
        }
        return image.encodeToData(EncodedImageFormat.PNG)!!.bytes
    }

    @Test
    fun `bracket with two players matches snapshot`() {
        val bracket = TournamentBracket(
            rounds = listOf(
                TournamentRound(
                    listOf(
                        TournamentMatch(
                            entry1 = entry(1, "Alice", 800.0, CardRarity.SSR),
                            entry2 = entry(2, "Bob", 500.0, CardRarity.SR),
                            winnerId = 1,
                        ),
                    ),
                ),
            ),
            winnerId = 1,
            winner = entry(1, "Alice", 800.0, CardRarity.SSR),
        )
        assertMatchesSnapshot("bracket-2-players", render(bracket, "Two Player Cup"))
    }

    @Test
    fun `bracket with four players matches snapshot`() {
        val bracket = TournamentBracket(
            rounds = listOf(
                TournamentRound(
                    listOf(
                        TournamentMatch(
                            entry1 = entry(1, "Alpha", 900.0, CardRarity.EX),
                            entry2 = entry(4, "Delta", 300.0, CardRarity.R),
                            winnerId = 1,
                        ),
                        TournamentMatch(
                            entry1 = entry(2, "Beta", 700.0, CardRarity.SSR),
                            entry2 = entry(3, "Gamma", 500.0, CardRarity.SR),
                            winnerId = 3,
                        ),
                    ),
                ),
                TournamentRound(
                    listOf(
                        TournamentMatch(
                            entry1 = entry(1, "Alpha", 900.0, CardRarity.EX),
                            entry2 = entry(3, "Gamma", 500.0, CardRarity.SR),
                            winnerId = 1,
                        ),
                    ),
                ),
            ),
            winnerId = 1,
            winner = entry(1, "Alpha", 900.0, CardRarity.EX),
        )
        assertMatchesSnapshot("bracket-4-players", render(bracket, "Four Player Open"))
    }

    @Test
    fun `bracket with bye matches snapshot`() {
        val bracket = TournamentBracket(
            rounds = listOf(
                TournamentRound(
                    listOf(
                        TournamentMatch(
                            entry1 = entry(1, "TopSeed", 1000.0, CardRarity.EX),
                            entry2 = entry(2, "Runner", 400.0, CardRarity.SR),
                            winnerId = 1,
                        ),
                        TournamentMatch(
                            entry1 = entry(3, "Third", 600.0, CardRarity.SSR),
                            entry2 = null,
                            winnerId = 3,
                        ),
                    ),
                ),
                TournamentRound(
                    listOf(
                        TournamentMatch(
                            entry1 = entry(1, "TopSeed", 1000.0, CardRarity.EX),
                            entry2 = entry(3, "Third", 600.0, CardRarity.SSR),
                            winnerId = 3,
                        ),
                    ),
                ),
            ),
            winnerId = 3,
            winner = entry(3, "Third", 600.0, CardRarity.SSR),
        )
        assertMatchesSnapshot("bracket-with-bye", render(bracket, "Bye Round Championship"))
    }

    @Test
    fun `bracket with eight players matches snapshot`() {
        val e1 = entry(1, "WhiteCat", 9000.0, CardRarity.EX)
        val e2 = entry(2, "Vaxei", 7000.0, CardRarity.EX)
        val e3 = entry(3, "Rafis", 6000.0, CardRarity.EX)
        val e4 = entry(4, "Mrekk", 5000.0, CardRarity.EX)
        val e5 = entry(5, "Akolibed", 4500.0, CardRarity.SSR)
        val e6 = entry(6, "maliszewski", 3500.0, CardRarity.SSR)
        val e7 = entry(7, "Lifeline", 2000.0, CardRarity.SR)
        val e8 = entry(8, "RyuK", 1500.0, CardRarity.SR)

        val bracket = TournamentBracket(
            rounds = listOf(
                TournamentRound(
                    listOf(
                        TournamentMatch(entry1 = e1, entry2 = e8, winnerId = 1),
                        TournamentMatch(entry1 = e4, entry2 = e5, winnerId = 5),
                        TournamentMatch(entry1 = e3, entry2 = e6, winnerId = 3),
                        TournamentMatch(entry1 = e2, entry2 = e7, winnerId = 2),
                    ),
                ),
                TournamentRound(
                    listOf(
                        TournamentMatch(entry1 = e1, entry2 = e5, winnerId = 1),
                        TournamentMatch(entry1 = e3, entry2 = e2, winnerId = 2),
                    ),
                ),
                TournamentRound(
                    listOf(
                        TournamentMatch(entry1 = e1, entry2 = e2, winnerId = 1),
                    ),
                ),
            ),
            winnerId = 1,
            winner = e1,
        )
        assertMatchesSnapshot("bracket-8-players", render(bracket, "osu! Grand Championship"))
    }

    @Test
    fun `bracket with foil winner card matches snapshot`() {
        val bracket = TournamentBracket(
            rounds = listOf(
                TournamentRound(
                    listOf(
                        TournamentMatch(
                            entry1 = entry(1, "FoilKing", 800.0, CardRarity.EX, foil = true),
                            entry2 = entry(2, "Challenger", 600.0, CardRarity.SSR),
                            winnerId = 1,
                        ),
                    ),
                ),
            ),
            winnerId = 1,
            winner = entry(1, "FoilKing", 800.0, CardRarity.EX, foil = true),
        )
        assertMatchesSnapshot("bracket-foil-winner", render(bracket, "Foil Showcase"))
    }

    @Test
    fun `bracket with mixed conditions matches snapshot`() {
        val bracket = TournamentBracket(
            rounds = listOf(
                TournamentRound(
                    listOf(
                        TournamentMatch(
                            entry1 = entry(1, "MintPlayer", 900.0, CardRarity.SSR, condition = CardCondition.Mint),
                            entry2 = entry(2, "DamagedCard", 200.0, CardRarity.SSR, condition = CardCondition.Damaged),
                            winnerId = 2,
                        ),
                    ),
                ),
            ),
            winnerId = 2,
            winner = entry(2, "DamagedCard", 200.0, CardRarity.SSR, condition = CardCondition.Damaged),
        )
        assertMatchesSnapshot("bracket-mixed-conditions", render(bracket, "Underdog Victory"))
    }

    @Test
    fun `bracket with single entry matches snapshot`() {
        val soloEntry = entry(1, "OnlyPlayer", 500.0, CardRarity.SSR)
        val bracket = TournamentBracket(
            rounds = listOf(
                TournamentRound(
                    listOf(
                        TournamentMatch(
                            entry1 = soloEntry,
                            entry2 = null,
                            winnerId = 1,
                        ),
                    ),
                ),
            ),
            winnerId = 1,
            winner = soloEntry,
        )
        assertMatchesSnapshot("bracket-1-player", render(bracket, "Solo Entry"))
    }

    @Test
    fun `bracket with no entries matches snapshot`() {
        val bracket = TournamentBracket(
            rounds = emptyList(),
            winnerId = null,
        )
        assertMatchesSnapshot("bracket-0-players", render(bracket, "Empty Tournament"))
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
