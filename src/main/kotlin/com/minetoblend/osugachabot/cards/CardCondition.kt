package com.minetoblend.osugachabot.cards

enum class CardCondition {
    Mint,
    Good,
    Poor,
    Damaged
}

fun CardCondition.multiplier(): Float = when (this) {
    Mint -> 1f
    Good -> 0.5f
    Poor -> 0.2f
    Damaged -> 0.1f
}