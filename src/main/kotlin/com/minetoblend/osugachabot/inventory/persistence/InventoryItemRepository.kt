package com.minetoblend.osugachabot.inventory.persistence

import com.minetoblend.osugachabot.inventory.ItemType
import org.springframework.data.jpa.repository.JpaRepository

interface InventoryItemRepository : JpaRepository<InventoryItemEntity, Long> {
    fun findByUserIdAndItemType(userId: Long, itemType: ItemType): InventoryItemEntity?
}
