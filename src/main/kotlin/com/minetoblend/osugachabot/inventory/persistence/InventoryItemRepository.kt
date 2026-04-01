package com.minetoblend.osugachabot.inventory.persistence

import com.minetoblend.osugachabot.inventory.ItemType
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface InventoryItemRepository : JpaRepository<InventoryItemEntity, Long> {
    fun findByUserIdAndItemType(userId: Long, itemType: ItemType): InventoryItemEntity?
    fun findByUserId(userId: Long): List<InventoryItemEntity>
    fun findByItemTypeOrderByAmountDesc(itemType: ItemType, pageable: Pageable): Page<InventoryItemEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItemEntity i WHERE i.userId = :userId AND i.itemType = :itemType")
    fun findByUserIdAndItemTypeForUpdate(userId: Long, itemType: ItemType): InventoryItemEntity?

    @Modifying
    @Query(
        value = "INSERT INTO inventory_items (user_id, item_type, amount) VALUES (:userId, :#{#itemType.name()}, 0) ON DUPLICATE KEY UPDATE id = id",
        nativeQuery = true,
    )
    fun insertIgnore(userId: Long, itemType: ItemType)
}
