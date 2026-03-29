package com.minetoblend.osugachabot.inventory

import com.minetoblend.osugachabot.users.UserId

interface InventoryService {
    fun getItem(userId: UserId, itemType: ItemType): InventoryItem

    fun addItems(userId: UserId, itemType: ItemType, amount: Long)

    fun removeItems(userId: UserId, itemType: ItemType, amount: Long): RemoveItemsResult
}

sealed interface RemoveItemsResult {
    data object Success : RemoveItemsResult
    data object InsufficientItems : RemoveItemsResult
}
