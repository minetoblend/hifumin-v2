package com.minetoblend.osugachabot.cards

enum class CardRarity {
    Common,
    Uncommon,
    Rare,
    Legendary,
    Mythic;

    companion object {
        fun fromFollowerCount(followerCount: Int): CardRarity = when {
            followerCount >= 50_000 -> Mythic
            followerCount >= 10_000 -> Legendary
            followerCount >= 2_000  -> Rare
            followerCount >= 600    -> Uncommon
            else                    -> Common
        }
    }
}
