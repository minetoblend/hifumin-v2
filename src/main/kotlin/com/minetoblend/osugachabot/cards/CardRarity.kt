package com.minetoblend.osugachabot.cards

enum class CardRarity {
    N,
    R,
    SR,
    SSR,
    EX;

    companion object {
        fun fromFollowerCount(followerCount: Int): CardRarity = when {
            followerCount >= 8000 -> EX
            followerCount >= 850 -> SSR
            followerCount >= 280 -> SR
            followerCount >= 100 -> R
            else -> N
        }
    }
}
