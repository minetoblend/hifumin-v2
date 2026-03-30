package com.minetoblend.osugachabot.graphics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val holographicColors = listOf(
    Color(0xFFFF6B6B),
    Color(0xFFFFD93D),
    Color(0xFF6BCB77),
    Color(0xFF4D96FF),
    Color(0xFFBB6BD9),
    Color(0xFFFF6B6B),
)

@Composable
fun FoilOverlay(modifier: Modifier = Modifier) {
    val primaryBrush = Brush.linearGradient(
        colors = holographicColors.map { it.copy(alpha = 0.18f) },
        start = Offset(0f, 0f),
        end = Offset(1300f, 720f)
    )

    val secondaryBrush = Brush.linearGradient(
        colors = buildList {
            for (color in holographicColors) {
                add(color.copy(alpha = 0.1f))
                add(Color.Transparent)
            }
        },
        start = Offset(0f, 140f),
        end = Offset(1100f, 650f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(brush = primaryBrush, blendMode = BlendMode.Screen)
                drawRect(brush = secondaryBrush, blendMode = BlendMode.Screen)
            }
    )
}

val foilGlowColor = Color(0xFFBB6BD9)
