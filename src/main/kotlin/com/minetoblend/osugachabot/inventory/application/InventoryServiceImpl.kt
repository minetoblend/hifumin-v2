package com.minetoblend.osugachabot.inventory.application

import com.minetoblend.osugachabot.inventory.InventoryItem
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.inventory.RemoveItemsResult
import com.minetoblend.osugachabot.inventory.persistence.InventoryItemRepository
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryServiceImpl(
    private val inventoryItemRepository: InventoryItemRepository,
) : InventoryService {

    override fun getItem(userId: UserId, itemType: ItemType): InventoryItem {
        val entity = inventoryItemRepository.findByUserIdAndItemType(userId.value, itemType)
        return InventoryItem(userId, itemType, entity?.amount ?: 0L)
    }

    override fun getItems(userId: UserId): List<InventoryItem> {
        return inventoryItemRepository.findByUserId(userId.value)
            .filter { it.amount > 0 }
            .sortedBy { it.itemType.ordinal }
            .map { InventoryItem(userId, it.itemType, it.amount) }
    }

    override fun getGoldLeaderboard(pageable: Pageable): Page<InventoryItem> =
        inventoryItemRepository.findByItemTypeOrderByAmountDesc(ItemType.Gold, pageable)
            .map { InventoryItem(it.userId.toUserId(), it.itemType, it.amount) }

    @Transactional
    override fun addItems(userId: UserId, itemType: ItemType, amount: Long) {
        require(amount > 0) { "amount must be positive" }
        inventoryItemRepository.insertIgnore(userId.value, itemType)
        val entity = inventoryItemRepository.findByUserIdAndItemTypeForUpdate(userId.value, itemType)!!
        entity.amount += amount
    }

    @Transactional
    override fun removeItems(userId: UserId, itemType: ItemType, amount: Long): RemoveItemsResult {
        require(amount > 0) { "amount must be positive" }
        val entity = inventoryItemRepository.findByUserIdAndItemTypeForUpdate(userId.value, itemType)
            ?: return RemoveItemsResult.InsufficientItems

        if (entity.amount < amount) return RemoveItemsResult.InsufficientItems

        entity.amount -= amount
        return RemoveItemsResult.Success
    }
}
