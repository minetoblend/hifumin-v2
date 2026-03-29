package com.minetoblend.osugachabot.shop

import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.inventory.description

@JvmInline
value class ShopItemId(val value: String)

data class ShopItem(
    val id: ShopItemId,
    val item: ItemType,
    val name: String = item.name,
    val description: String? = item.description,
    val goldPrice: Int
)