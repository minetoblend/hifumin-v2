package com.minetoblend.osugachabot.inventory

import com.minetoblend.osugachabot.inventory.ItemType.DropSpeedup
import com.minetoblend.osugachabot.inventory.ItemType.Gold

enum class ItemType {
    Gold,
    DropSpeedup,
    SuperDrop,
}

val ItemType.icon: String
    get() = when (this) {
        Gold -> ":money_bag:"
        DropSpeedup -> ":clock1:"
        SuperDrop -> ":ticket:"
    }

val ItemType.prettyName: String
    get() = when (this) {
        Gold -> "Gold"
        DropSpeedup -> "Drop Speedup"
        SuperDrop -> "Superdrop"
    }

val ItemType.description: String?
    get() = when (this) {
        Gold -> "Gold"
        DropSpeedup -> "Drop Cooldown -50% (6h)"
        SuperDrop -> null
    }