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
import com.minetoblend.osugachabot.graphics.CardRenderer.Companion.CARD_HEIGHT
import com.minetoblend.osugachabot.graphics.CardRenderer.Companion.CARD_WIDTH

@Composable
@Preview
fun CardComposablePreview() {
    val card = Card(
        id = CardId(1),
        username = "Maarvin",
        countryCode = "JP",
        title = null,
        followerCount = 1000,
        globalRank = 1000,
        userId = 3,
        rarity = N,
    )

    CardComposable(card)
}

@Composable
fun CardComposable(
    card: Card,
    avatar: ImageBitmap? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val colors = CardColors.forRarity(card.rarity)

    Box(
        modifier = modifier
            .size(CARD_WIDTH.dp, CARD_HEIGHT.dp)
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = colors.glow,
                spotColor = colors.primary
            )
            .clip(shape)
            .background(Color(0xFF1A1A2E))
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.6f),
                        colors.secondary.copy(alpha = 0.3f),
                        colors.primary.copy(alpha = 0.1f),
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                ),
                shape = shape,
            )
    ) {
        CardBackground(colors)

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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = card.rarity.name.uppercase(),
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(colors.primary, colors.secondary)
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                )
            }

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
private fun CardBackground(colors: CardColors) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.15f),
                        colors.secondary.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset.Zero,
                    radius = 500f
                )
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors.secondary.copy(alpha = 0.05f),
                        colors.primary.copy(alpha = 0.05f),
                    ),
                    start = Offset.Infinite,
                    end = Offset.Zero
                )
            )
    )
}