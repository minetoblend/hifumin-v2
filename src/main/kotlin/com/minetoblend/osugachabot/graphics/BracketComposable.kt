package com.minetoblend.osugachabot.graphics

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minetoblend.osugachabot.generated.resources.Res
import com.minetoblend.osugachabot.generated.resources.default_avatar
import org.jetbrains.compose.resources.imageResource
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardRarity
import com.minetoblend.osugachabot.tournament.*

@Composable
@Preview
fun BracketComposablePreview() {
    fun previewEntry(userId: Long, username: String, weight: Double, rarity: CardRarity = CardRarity.SR) =
        TournamentMatchEntry(
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
                condition = CardCondition.Mint,
                foil = false,
            ),
            weight = weight,
        )

    val bracket = TournamentBracket(
        rounds = listOf(
            TournamentRound(
                listOf(
                    TournamentMatch(
                        entry1 = previewEntry(1, "WhiteCat", 9000.0, CardRarity.EX),
                        entry2 = previewEntry(8, "Lifeline", 2000.0, CardRarity.SSR),
                        winnerId = 1,
                    ),
                    TournamentMatch(
                        entry1 = previewEntry(4, "Mrekk", 5000.0, CardRarity.EX),
                        entry2 = previewEntry(5, "Akolibed", 4500.0, CardRarity.EX),
                        winnerId = 5,
                    ),
                    TournamentMatch(
                        entry1 = previewEntry(3, "Rafis", 6000.0, CardRarity.EX),
                        entry2 = previewEntry(6, "maliszewski", 3500.0, CardRarity.SSR),
                        winnerId = 3,
                    ),
                    TournamentMatch(
                        entry1 = previewEntry(2, "Vaxei", 7000.0, CardRarity.EX),
                        entry2 = null,
                        winnerId = 2,
                    ),
                ),
            ),
            TournamentRound(
                listOf(
                    TournamentMatch(
                        entry1 = previewEntry(1, "WhiteCat", 9000.0, CardRarity.EX),
                        entry2 = previewEntry(5, "Akolibed", 4500.0, CardRarity.EX),
                        winnerId = 1,
                    ),
                    TournamentMatch(
                        entry1 = previewEntry(3, "Rafis", 6000.0, CardRarity.EX),
                        entry2 = previewEntry(2, "Vaxei", 7000.0, CardRarity.EX),
                        winnerId = 2,
                    ),
                ),
            ),
            TournamentRound(
                listOf(
                    TournamentMatch(
                        entry1 = previewEntry(1, "WhiteCat", 9000.0, CardRarity.EX),
                        entry2 = previewEntry(2, "Vaxei", 7000.0, CardRarity.EX),
                        winnerId = 1,
                    ),
                ),
            ),
        ),
        winnerId = 1,
    )

    val size = computeBracketSize(bracket)

    BracketComposable(
        bracket = bracket,
        tournamentName = "hifumin World Cup",
        avatars = emptyMap(),
        modifier = Modifier.size(size.width.dp, size.height.dp),
    )
}

private val BRACKET_BG = Color(0xFF0D1117)
private val MATCH_BG = Color(0xFF161B22)
private val MATCH_BORDER = Color(0xFF30363D)
private val WINNER_BORDER = Color(0xFF3FB950)
private val CONNECTOR_COLOR = Color(0xFF30363D)
private val TEXT_PRIMARY = Color(0xFFE6EDF3)
private val TEXT_SECONDARY = Color(0xFF8B949E)
private val TEXT_WINNER = Color(0xFF3FB950)
private val BYE_TEXT = Color(0xFF484F58)

const val MINI_CARD_WIDTH = 83
const val MINI_CARD_HEIGHT = 125
const val MATCH_ENTRY_HEIGHT = 40
const val MATCH_WIDTH = 200
const val MATCH_HEIGHT = MATCH_ENTRY_HEIGHT * 2 + 4
const val ROUND_SPACING = 80
const val MATCH_VERTICAL_SPACING = 24
const val ROUND_LABEL_HEIGHT = 20
const val TITLE_HEIGHT = 40  // 20sp text + 16dp bottom padding, rounded up
const val BRACKET_PADDING = 24

@Composable
fun BracketComposable(
    bracket: TournamentBracket,
    tournamentName: String,
    avatars: Map<Long, ImageBitmap?>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(BRACKET_BG)
            .padding(BRACKET_PADDING.dp),
    ) {
        // Title
        Text(
            text = tournamentName,
            style = TextStyle(
                color = TEXT_PRIMARY,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Fonts.InterSansSerif,
                letterSpacing = 0.5.sp,
            ),
            modifier = Modifier.height(TITLE_HEIGHT.dp),
        )

        if (bracket.rounds.isEmpty() && bracket.winner == null) return@Column

        // Lay out the bracket using a Box with absolute positioning via Canvas for connectors
        // and Column/Row for the match boxes
        Box {
            // Draw connector lines on a full-size canvas behind everything
            Canvas(modifier = Modifier.matchParentSize()) {
                val dpToPx = { dp: Int -> dp.dp.toPx() }
                val roundLabelHeight = dpToPx(ROUND_LABEL_HEIGHT)

                fun matchCenterY(roundIndex: Int, matchIndex: Int): Float {
                    val offset = dpToPx(roundTopOffset(roundIndex))
                    val spacing = dpToPx(matchSpacingForRound(roundIndex))
                    val matchH = dpToPx(MATCH_HEIGHT)
                    return roundLabelHeight + offset + matchIndex * (matchH + spacing) + matchH / 2f
                }

                for (roundIndex in 0 until bracket.rounds.size - 1) {
                    val round = bracket.rounds[roundIndex]
                    val roundX = roundIndex * (dpToPx(MATCH_WIDTH) + dpToPx(ROUND_SPACING))
                    val nextRoundX = (roundIndex + 1) * (dpToPx(MATCH_WIDTH) + dpToPx(ROUND_SPACING))

                    for (matchIndex in round.matches.indices step 2) {
                        val topMatchY = matchCenterY(roundIndex, matchIndex)
                        val bottomMatchY = if (matchIndex + 1 < round.matches.size) {
                            matchCenterY(roundIndex, matchIndex + 1)
                        } else {
                            topMatchY
                        }

                        val nextMatchIndex = matchIndex / 2
                        val nextMatchY = matchCenterY(roundIndex + 1, nextMatchIndex)

                        val lineStartX = roundX + dpToPx(MATCH_WIDTH)
                        val lineEndX = nextRoundX
                        val midX = (lineStartX + lineEndX) / 2f

                        // Horizontal from top match to midpoint
                        drawLine(CONNECTOR_COLOR, Offset(lineStartX, topMatchY), Offset(midX, topMatchY), strokeWidth = 1.5f)
                        // Horizontal from bottom match to midpoint
                        if (bottomMatchY != topMatchY) {
                            drawLine(CONNECTOR_COLOR, Offset(lineStartX, bottomMatchY), Offset(midX, bottomMatchY), strokeWidth = 1.5f)
                        }
                        // Vertical connecting the pair
                        drawLine(CONNECTOR_COLOR, Offset(midX, topMatchY), Offset(midX, bottomMatchY), strokeWidth = 1.5f)
                        // Horizontal from midpoint vertical center to next match
                        val midY = (topMatchY + bottomMatchY) / 2f
                        drawLine(CONNECTOR_COLOR, Offset(midX, midY), Offset(lineEndX, nextMatchY), strokeWidth = 1.5f)
                    }
                }
            }

            // Render round columns
            Row {
                bracket.rounds.forEachIndexed { roundIndex, round ->
                    val isLastRound = roundIndex == bracket.rounds.lastIndex

                    // Center each round's matches relative to the first round
                    val topPadding = roundTopOffset(roundIndex)

                    Column(
                        modifier = Modifier.width(MATCH_WIDTH.dp),
                    ) {
                        // Round label
                        Text(
                            text = roundLabel(roundIndex, bracket.rounds.size),
                            style = TextStyle(
                                color = TEXT_SECONDARY,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = Fonts.InterSansSerif,
                                letterSpacing = 1.sp,
                            ),
                            modifier = Modifier.height(ROUND_LABEL_HEIGHT.dp),
                        )

                        if (topPadding > 0) {
                            Spacer(modifier = Modifier.height(topPadding.dp))
                        }

                        round.matches.forEachIndexed { matchIndex, match ->
                            if (matchIndex > 0) {
                                Spacer(modifier = Modifier.height(matchSpacingForRound(roundIndex).dp))
                            }
                            MatchComposable(match, bracket.winnerId, avatars)
                        }
                    }

                    if (!isLastRound) {
                        Spacer(modifier = Modifier.width(ROUND_SPACING.dp))
                    }
                }

                // Winner display
                val winnerEntry = bracket.winner
                if (winnerEntry != null) {
                    // Align card center to the final match center
                    val lastRoundIndex = (bracket.rounds.lastIndex).coerceAtLeast(0)
                    val finalMatchCenter = if (bracket.rounds.isNotEmpty()) {
                        roundTopOffset(lastRoundIndex) + MATCH_HEIGHT / 2
                    } else {
                        MINI_CARD_HEIGHT / 2
                    }
                    val winnerTopPadding = (finalMatchCenter - MINI_CARD_HEIGHT / 2).coerceAtLeast(0)

                    if (bracket.rounds.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "WINNER",
                            style = TextStyle(
                                color = TEXT_WINNER,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = Fonts.InterSansSerif,
                                letterSpacing = 1.sp,
                            ),
                            modifier = Modifier.height(ROUND_LABEL_HEIGHT.dp),
                        )

                        if (winnerTopPadding > 0) {
                            Spacer(modifier = Modifier.height(winnerTopPadding.dp))
                        }

                        MiniCardComposable(
                            entry = winnerEntry,
                            isWinner = true,
                            avatar = avatars[winnerEntry.cardReplica.card.userId],
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchComposable(
    match: TournamentMatch,
    bracketWinnerId: Long?,
    avatars: Map<Long, ImageBitmap?>,
) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier = Modifier
            .width(MATCH_WIDTH.dp)
            .clip(shape)
            .background(MATCH_BG)
            .border(1.dp, MATCH_BORDER, shape),
    ) {
        MatchEntryRow(match.entry1, isWinner = match.entry1?.userId == match.winnerId, avatars)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MATCH_BORDER),
        )
        MatchEntryRow(match.entry2, isWinner = match.entry2?.userId == match.winnerId, avatars)
    }
}

@Composable
private fun MatchEntryRow(
    entry: TournamentMatchEntry?,
    isWinner: Boolean,
    avatars: Map<Long, ImageBitmap?>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MATCH_ENTRY_HEIGHT.dp)
            .then(
                if (isWinner) Modifier.background(WINNER_BORDER.copy(alpha = 0.1f))
                else Modifier
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (entry != null) {
            val card = entry.cardReplica.card
            val avatar = avatars[card.userId] ?: imageResource(Res.drawable.default_avatar)

            // Avatar
            Image(
                bitmap = avatar,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, MATCH_BORDER, CircleShape),
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Player name
            Text(
                text = card.username,
                style = TextStyle(
                    color = if (isWinner) TEXT_WINNER else TEXT_PRIMARY,
                    fontSize = 12.sp,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = Fonts.InterSansSerif,
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )

            // Weight indicator
            Text(
                text = String.format("%.0f", entry.weight),
                style = TextStyle(
                    color = TEXT_SECONDARY,
                    fontSize = 10.sp,
                    fontFamily = Fonts.JetBrainsMono,
                ),
            )

            if (isWinner) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "W",
                    style = TextStyle(
                        color = TEXT_WINNER,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = Fonts.JetBrainsMono,
                    ),
                )
            }
        } else {
            Text(
                text = "BYE",
                style = TextStyle(
                    color = BYE_TEXT,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Fonts.InterSansSerif,
                    letterSpacing = 1.sp,
                ),
            )
        }
    }
}

@Composable
private fun MiniCardComposable(
    entry: TournamentMatchEntry,
    isWinner: Boolean,
    avatar: ImageBitmap?,
) {
    val card = entry.cardReplica.card.toDomain()
    val foil = entry.cardReplica.foil
    val scale = MINI_CARD_WIDTH.toFloat() / CardRenderer.CARD_WIDTH.toFloat()

    Box(
        modifier = Modifier
            .size(MINI_CARD_WIDTH.dp, MINI_CARD_HEIGHT.dp)
            .then(
                if (isWinner) Modifier.border(
                    2.dp,
                    WINNER_BORDER,
                    RoundedCornerShape((12 * scale).dp),
                ) else Modifier
            )
            .clip(RoundedCornerShape((12 * scale).dp))
            .wrapContentSize(unbounded = true, align = Alignment.TopStart),
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                },
        ) {
            CardComposable(
                card = card,
                avatar = avatar,
                foil = foil,
            )
        }
    }
}

private fun roundLabel(roundIndex: Int, totalRounds: Int): String {
    val roundsFromEnd = totalRounds - roundIndex
    return when (roundsFromEnd) {
        1 -> "FINAL"
        2 -> "SEMIFINAL"
        3 -> "QUARTERFINAL"
        else -> "ROUND ${roundIndex + 1}"
    }
}

private fun matchSpacingForRound(roundIndex: Int): Int {
    // Matches space out more as rounds progress to align with the bracket shape
    return MATCH_VERTICAL_SPACING + (MATCH_HEIGHT + MATCH_VERTICAL_SPACING) * ((1 shl roundIndex) - 1)
}

private fun roundTopOffset(roundIndex: Int): Int {
    if (roundIndex == 0) return 0
    // Each subsequent round's first match should be centered between the first pair of the previous round
    // The offset is half of (match height + spacing of the previous round)
    val prevSpacing = matchSpacingForRound(roundIndex - 1)
    return (MATCH_HEIGHT + prevSpacing) / 2 + roundTopOffset(roundIndex - 1)
}

data class BracketSize(val width: Int, val height: Int)

fun computeBracketSize(bracket: TournamentBracket): BracketSize {
    val rounds = bracket.rounds.size
    val firstRoundMatches = bracket.rounds.firstOrNull()?.matches?.size ?: 0
    val width = (rounds * (MATCH_WIDTH + ROUND_SPACING) + MINI_CARD_WIDTH + 48 + 48).coerceAtLeast(400)
    val firstRoundHeight = if (firstRoundMatches > 0) {
        firstRoundMatches * (MATCH_HEIGHT + MATCH_VERTICAL_SPACING) - MATCH_VERTICAL_SPACING
    } else {
        0
    }

    // Winner card column height: label + offset + card
    val winnerColumnHeight = if (bracket.winner != null) {
        val finalMatchCenter = if (rounds > 0) {
            roundTopOffset(rounds - 1) + MATCH_HEIGHT / 2
        } else {
            MINI_CARD_HEIGHT / 2
        }
        val winnerTopPadding = (finalMatchCenter - MINI_CARD_HEIGHT / 2).coerceAtLeast(0)
        ROUND_LABEL_HEIGHT + winnerTopPadding + MINI_CARD_HEIGHT
    } else {
        0
    }

    val contentHeight = maxOf(firstRoundHeight + ROUND_LABEL_HEIGHT, winnerColumnHeight)
    val height = (contentHeight + TITLE_HEIGHT + BRACKET_PADDING * 2).coerceAtLeast(200)
    return BracketSize(width, height)
}
