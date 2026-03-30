package com.minetoblend.osugachabot.graphics

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.CardRarity
import com.minetoblend.osugachabot.graphics.CardRenderer.Companion.CARD_HEIGHT
import com.minetoblend.osugachabot.graphics.CardRenderer.Companion.CARD_WIDTH

@Composable
@Preview
fun CardComposablePreview() {
    val card = Card(
        id = CardId(1),
        username = "Maarvin",
        countryCode = "AT",
        title = null,
        followerCount = 1000,
        globalRank = 1000,
        userId = 3,
        rarity = SR,
    )

    CardComposable(card, foil = true, modifier = Modifier.padding(16.dp))
}

@Composable
fun CardComposable(
    card: Card,
    avatar: ImageBitmap? = null,
    foil: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val colors = CardColors.forRarity(card.rarity)
    val isEpic = card.rarity == CardRarity.SSR || card.rarity == CardRarity.EX

    val elevation = if (foil) 28.dp else if (isEpic) 24.dp else 12.dp
    val borderWidth = if (isEpic) 3.dp else 2.dp
    val borderAlphas = if (isEpic) Triple(0.9f, 0.6f, 0.3f) else Triple(0.6f, 0.3f, 0.1f)

    val glowColor = if (foil) foilGlowColor else colors.glow
    val glowAlpha = if (foil) 0.9f else if (isEpic) 0.8f else 0.5f

    Box(
        modifier = modifier
            .size(CARD_WIDTH.dp, CARD_HEIGHT.dp)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = glowColor.copy(alpha = glowAlpha),
                spotColor = if (foil) foilGlowColor else colors.primary
            )
            .clip(shape)
            .background(Color(0xFF1A1A2E))
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = borderAlphas.first),
                        colors.secondary.copy(alpha = borderAlphas.second),
                        colors.primary.copy(alpha = borderAlphas.third),
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                ),
                shape = shape,
            )
    ) {
        CardBackground(colors, isEpic)

        if (foil) {
            FoilOverlay()
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CardHeader(card, colors)
            CardImage(avatar)
            Spacer(modifier = Modifier.weight(1f))
            CardFooter(card, colors)
        }
    }
}

@Composable
private fun CardHeader(card: Card, colors: CardColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.username,
                    style = TextStyle(
                        color = colors.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.4.sp
                    ),
                    maxLines = 1
                )
            }
        }

        val isEpicRarity = card.rarity == CardRarity.SSR || card.rarity == CardRarity.EX

        Surface(
                color = if (isEpicRarity) colors.primary.copy(alpha = 0.12f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = card.rarity.name.uppercase(),
                    modifier = if (isEpicRarity) Modifier.padding(horizontal = 6.dp, vertical = 2.dp) else Modifier,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(colors.primary, colors.secondary)
                        ),
                        fontSize = if (isEpicRarity) 18.sp else 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = if (isEpicRarity) 1.2.sp else 0.8.sp
                    )
                )
            }

//        if (card is CardInfo.UserCard) {
//            androidx.compose.material.Text(
//                text = "#${card.cardId}",
//                style = TextStyle(
//                    color = colors.onSurfaceVariant,
//                    fontSize = 11.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    fontFamily = FontFamily.Monospace,
//                    letterSpacing = 0.2.sp
//                )
//            )
//        }
    }
}

@Composable
private fun CardImage(avatar: ImageBitmap?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.Black.copy(alpha = 0.3f))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        if (avatar != null) {
            Image(
                bitmap = avatar,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                        startY = 0.6f
                    )
                )
        )
    }
}

@Composable
private fun CardFooter(card: Card, colors: CardColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            if (card.globalRank != null) {
                Column {
                    Text(
                        text = "GLOBAL RANK",
                        style = TextStyle(
                            color = colors.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Default,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "#${String.format("%,d", card.globalRank)}",
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Default,
                            letterSpacing = 0.2.sp
                        )
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "FOLLOWERS",
                style = TextStyle(
                    color = colors.onSurfaceVariant,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = String.format("%,d", card.followerCount),
                style = TextStyle(
                    color = colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Default,
                    letterSpacing = 0.2.sp
                )
            )
        }
    }
}

@Composable
private fun CardBackground(colors: CardColors, isEpic: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = if (isEpic) 0.3f else 0.15f),
                        colors.secondary.copy(alpha = if (isEpic) 0.15f else 0.05f),
                        Color.Transparent
                    ),
                    center = Offset.Zero,
                    radius = if (isEpic) 700f else 500f
                )
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors.secondary.copy(alpha = if (isEpic) 0.12f else 0.05f),
                        colors.primary.copy(alpha = if (isEpic) 0.12f else 0.05f),
                    ),
                    start = Offset.Infinite,
                    end = Offset.Zero
                )
            )
    )

    if (isEpic) {
        // Additional radial glow from the bottom-right corner
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.secondary.copy(alpha = 0.2f),
                            colors.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(Float.MAX_VALUE, Float.MAX_VALUE),
                        radius = 600f
                    )
                )
        )

        // Vertical shimmer accent
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.08f),
                            Color.Transparent,
                            colors.secondary.copy(alpha = 0.06f),
                            Color.Transparent,
                            colors.primary.copy(alpha = 0.08f),
                        )
                    )
                )
        )
    }
}