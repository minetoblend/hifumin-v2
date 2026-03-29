package com.minetoblend.osugachabot.inventory

import com.minetoblend.osugachabot.inventory.ItemType.DropSpeedup
import com.minetoblend.osugachabot.inventory.ItemType.Gold

enum class ItemType {
    Gold,
    DropSpeedup,
}

val ItemType.icon: String
    get() = when (this) {
        Gold -> ":money_bag:"
        DropSpeedup -> ":clock1:"
    }

val ItemType.prettyName: String
    get() = when (this) {
        Gold -> "Gold"
        DropSpeedup -> "Drop Speedup"
    }

val ItemType.description: String?
    get() = when (this) {
        Gold -> "Gold"
        DropSpeedup -> "Drop Cooldown -50% (6h)"
    }