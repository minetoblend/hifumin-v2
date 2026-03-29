package com.minetoblend.osugachabot.cards

enum class CardCondition {
    Mint,
    Good,
    Poor,
    Damaged
}

fun CardCondition.prettyName(): String = when (this) {
    Mint -> "Mint"
    Good -> "Good"
    Poor -> "Poor"
    Damaged -> "Badly damaged"
}

val CardCondition.icon: String
    get() = when (this) {
        Mint -> "✨"
        Good -> "\uD83D\uDC4D"
        Poor -> "\uD83D\uDE15"
        else -> "\uD83E\uDE79"
    }