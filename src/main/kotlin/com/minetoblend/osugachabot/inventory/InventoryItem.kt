package com.minetoblend.osugachabot.inventory

import com.minetoblend.osugachabot.users.UserId

data class InventoryItem(
    val userId: UserId,
    val itemType: ItemType,
    val amount: Long,
)

val InventoryItem.icon get() = itemType.icon
val InventoryItem.name get() = itemType.prettyName
val InventoryItem.description get() = itemType.description