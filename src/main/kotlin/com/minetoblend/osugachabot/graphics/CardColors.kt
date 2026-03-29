package com.minetoblend.osugachabot.graphics

import androidx.compose.ui.graphics.Color
import com.minetoblend.osugachabot.cards.CardRarity

val baseBackground = Color(0xFF222228)

data class CardColors(
    val primary: Color,
    val secondary: Color = primary,
    val onPrimary: Color = Color.White,
    val background: Color = baseBackground,
    val surface: Color = background.copy(alpha = 0.5f),
    val onSurface: Color = Color.White.copy(alpha = 0.9f),
    val onSurfaceVariant: Color = Color.White.copy(alpha = 0.5f),
    val border: Color = primary.copy(alpha = 0.2f),
    val glow: Color = primary,
) {
    companion object {
        fun forRarity(rarity: CardRarity) = when (rarity) {
            Common -> CardColors(
                primary = Color(0xFFBBBBBB),
                secondary = Color(0xFF888888),
            )

            Uncommon -> CardColors(
                primary = Color(0xFF3DF37A),
                secondary = Color(0xFF3DCBF3),
            )

            Rare -> CardColors(
                primary = Color(0xFFF58E7B),
                secondary = Color(0xFFF57BA2),
            )

            Legendary -> CardColors(
                primary = Color(0xFFF3A93D),
                secondary = Color(0xFFF33D3D),
            )

            Mythic -> CardColors(
                primary = Color(0xFFF33DA9),
                secondary = Color(0xFF8A3DF3),
            )
        }
    }
}