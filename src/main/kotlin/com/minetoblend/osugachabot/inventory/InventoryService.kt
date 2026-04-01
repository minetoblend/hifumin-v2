package com.minetoblend.osugachabot.inventory

import com.minetoblend.osugachabot.users.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface InventoryService {
    fun getItem(userId: UserId, itemType: ItemType): InventoryItem

    fun getItems(userId: UserId): List<InventoryItem>

    fun getGoldLeaderboard(pageable: Pageable): Page<InventoryItem>

    fun addItems(userId: UserId, itemType: ItemType, amount: Long)

    fun removeItems(userId: UserId, itemType: ItemType, amount: Long): RemoveItemsResult
}

sealed interface RemoveItemsResult {
    data object Success : RemoveItemsResult
    data object InsufficientItems : RemoveItemsResult
}
