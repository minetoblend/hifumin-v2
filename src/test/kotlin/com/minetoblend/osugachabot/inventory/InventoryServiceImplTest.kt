package com.minetoblend.osugachabot.inventory

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class InventoryServiceImplTest {

    @Autowired
    private lateinit var inventoryService: InventoryService

    @Test
    fun `getItem returns zero amount when no items exist`() {
        val item = inventoryService.getItem(UserId(900001L), ItemType.Gold)

        assertEquals(0L, item.amount)
        assertEquals(ItemType.Gold, item.itemType)
    }

    @Test
    fun `addItems creates entry and getItem returns correct amount`() {
        val userId = UserId(900002L)

        inventoryService.addItems(userId, ItemType.Gold, 50L)

        assertEquals(50L, inventoryService.getItem(userId, ItemType.Gold).amount)
    }

    @Test
    fun `addItems accumulates across multiple calls`() {
        val userId = UserId(900003L)

        inventoryService.addItems(userId, ItemType.Gold, 30L)
        inventoryService.addItems(userId, ItemType.Gold, 20L)

        assertEquals(50L, inventoryService.getItem(userId, ItemType.Gold).amount)
    }

    @Test
    fun `removeItems reduces amount and returns Success`() {
        val userId = UserId(900004L)
        inventoryService.addItems(userId, ItemType.Gold, 100L)

        val result = inventoryService.removeItems(userId, ItemType.Gold, 40L)

        assertEquals(RemoveItemsResult.Success, result)
        assertEquals(60L, inventoryService.getItem(userId, ItemType.Gold).amount)
    }

    @Test
    fun `removeItems returns InsufficientItems when not enough`() {
        val userId = UserId(900005L)
        inventoryService.addItems(userId, ItemType.Gold, 10L)

        val result = inventoryService.removeItems(userId, ItemType.Gold, 50L)

        assertEquals(RemoveItemsResult.InsufficientItems, result)
        assertEquals(10L, inventoryService.getItem(userId, ItemType.Gold).amount)
    }

    @Test
    fun `removeItems returns InsufficientItems when no items exist`() {
        val result = inventoryService.removeItems(UserId(900006L), ItemType.Gold, 1L)

        assertEquals(RemoveItemsResult.InsufficientItems, result)
    }
}
