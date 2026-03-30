package com.minetoblend.osugachabot.graphics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import com.minetoblend.osugachabot.generated.resources.Res
import com.minetoblend.osugachabot.generated.resources.foil
import org.jetbrains.compose.resources.painterResource


@Composable
fun FoilOverlay(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.foil),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                blendMode = BlendMode.Multiply
                alpha = 0.5f
            }
    )
    Image(
        painter = painterResource(Res.drawable.foil),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                blendMode = BlendMode.Overlay
                alpha = 0.5f
            }
    )
}

val foilGlowColor = Color(0xFFBB6BD9)
