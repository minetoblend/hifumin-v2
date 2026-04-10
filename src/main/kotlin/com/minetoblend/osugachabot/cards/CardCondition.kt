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

val CardCondition.upgradeCost: Long
    get() = when (this) {
        Damaged -> 150L
        Poor -> 400L
        Good -> 1000L
        Mint -> error("Mint cards cannot be upgraded")
    }

val CardCondition.upgradeSuccessRate: Double
    get() = when (this) {
        Damaged -> 0.60
        Poor -> 0.40
        Good -> 0.20
        Mint -> error("Mint cards cannot be upgraded")
    }

/**
 * Number of consecutive failed upgrade attempts (from this condition) after which
 * the next upgrade attempt is guaranteed to succeed.
 *
 * Harder upgrades have a more generous pity threshold.
 */
val CardCondition.upgradePityThreshold: Int
    get() = when (this) {
        Damaged -> 5
        Poor -> 7
        Good -> 10
        Mint -> error("Mint cards cannot be upgraded")
    }

fun CardCondition.nextCondition(): CardCondition = when (this) {
    Damaged -> Poor
    Poor -> Good
    Good -> Mint
    Mint -> error("Mint cards cannot be upgraded")
}