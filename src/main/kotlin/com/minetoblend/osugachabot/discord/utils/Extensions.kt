package com.minetoblend.osugachabot.discord.utils

import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kord.rest.builder.interaction.string

fun BaseInputChatBuilder.cardId(
    name: String,
    required: Boolean,
    description: String = "The card ID (e.g. aaaa)",
    block: StringChoiceBuilder.() -> Unit = {}
) {
    string(name, description) {
        minLength = 4
        maxLength = 5
        this.required = required

        block()
    }
}