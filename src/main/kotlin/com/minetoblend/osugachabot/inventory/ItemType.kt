package com.minetoblend.osugachabot.inventory

import com.minetoblend.osugachabot.inventory.ItemType.DropSpeedup
import com.minetoblend.osugachabot.inventory.ItemType.FreeClaim
import com.minetoblend.osugachabot.inventory.ItemType.Gold

enum class ItemType {
    Gold,
    DropSpeedup,
    SuperDrop,
    FreeClaim,
}

val ItemType.icon: String
    get() = when (this) {
        Gold -> ":money_bag:"
        DropSpeedup -> ":clock1:"
        SuperDrop -> ":ticket:"
        FreeClaim -> ":free:"
    }

val ItemType.prettyName: String
    get() = when (this) {
        Gold -> "Gold"
        DropSpeedup -> "Drop Speedup"
        SuperDrop -> "Superdrop"
        FreeClaim -> "Free Claim"
    }

val ItemType.description: String?
    get() = when (this) {
        Gold -> "Gold"
        DropSpeedup -> "Drop Cooldown -50% (6h)"
        SuperDrop -> null
        FreeClaim -> "Skip the next claim cooldown"
    }