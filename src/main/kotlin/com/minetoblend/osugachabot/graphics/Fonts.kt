package com.minetoblend.osugachabot.graphics

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.minetoblend.osugachabot.generated.resources.Res
import com.minetoblend.osugachabot.generated.resources.inter_extrabold
import com.minetoblend.osugachabot.generated.resources.inter_semibold
import com.minetoblend.osugachabot.generated.resources.jetbrainsmono_extrabold
import org.jetbrains.compose.resources.Font

object
Fonts {
    val InterSansSerif
        @Composable get() = FontFamily(
            Font(Res.font.inter_semibold, weight = FontWeight.SemiBold),
            Font(Res.font.inter_extrabold, weight = FontWeight.ExtraBold),
        )

    val JetBrainsMono
        @Composable get() = FontFamily(
            Font(Res.font.jetbrainsmono_extrabold, weight = FontWeight.ExtraBold),
        )
}