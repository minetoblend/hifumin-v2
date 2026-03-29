package com.minetoblend.osugachabot.cards

enum class CardRarity {
    Common,
    Uncommon,
    Rare,
    Legendary,
    Mythic;

    companion object {
        fun fromFollowerCount(followerCount: Int): CardRarity = when {
            followerCount >= 8000  -> Mythic
            followerCount >= 850   -> Legendary
            followerCount >= 280   -> Rare
            followerCount >= 100   -> Uncommon
            else                   -> Common
        }
    }
}
