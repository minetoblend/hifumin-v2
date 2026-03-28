package com.minetoblend.osugachabot.graphics

import androidx.compose.ui.graphics.Color

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
)