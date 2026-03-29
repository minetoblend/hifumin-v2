package com.minetoblend.osugachabot.inventory.persistence

import com.minetoblend.osugachabot.inventory.ItemType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "inventory_items",
    indexes = [
        Index(name = "idx_inventory_user_id", columnList = "user_id"),
        Index(name = "idx_inventory_user_item_type", columnList = "user_id, item_type", unique = true),
    ]
)
class InventoryItemEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val itemType: ItemType,

    @Column(nullable = false)
    var amount: Long,
)
