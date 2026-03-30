package com.minetoblend.osugachabot.graphics

import com.minetoblend.osugachabot.cards.Card
import com.minetoblend.osugachabot.cards.CardReplica
import com.minetoblend.osugachabot.drops.DroppedCard

data class RenderableCard(
    val card: Card,
    val foil: Boolean = false,
)

fun DroppedCard.toRenderableCard() = RenderableCard(card, foil)

fun CardReplica.toRenderableCard() = RenderableCard(card, foil)
