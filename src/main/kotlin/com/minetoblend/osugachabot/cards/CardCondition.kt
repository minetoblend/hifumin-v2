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