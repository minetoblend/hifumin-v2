package com.minetoblend.osugachabot.discord

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.AccessoryHolder
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder

interface ButtonId {
    fun toCustomId(): String
}

interface ButtonIdCompanion<T : ButtonId> {
    fun isValid(id: String): Boolean

    fun fromString(id: String): T?
}

fun ActionRowBuilder.interactionButton(
    style: ButtonStyle,
    id: ButtonId,
    builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit
) = interactionButton(style, id.toCustomId(), builder)

fun AccessoryHolder.interactionButtonAccessory(
    style: ButtonStyle,
    id: ButtonId,
    builder: ButtonBuilder.() -> Unit = {},
) {
    accessory = ButtonBuilder.InteractionButtonBuilder(style, id.toCustomId()).apply(builder)
}