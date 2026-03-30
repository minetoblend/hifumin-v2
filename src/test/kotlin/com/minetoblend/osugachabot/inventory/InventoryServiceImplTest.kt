package com.minetoblend.osugachabot.inventory

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
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

    @Test
    fun `getItems returns empty list when user has no items`() {
        val items = inventoryService.getItems(UserId(900007L))

        assertEquals(emptyList(), items)
    }

    @Test
    fun `getItems returns only items with amount greater than zero`() {
        val userId = UserId(900008L)
        inventoryService.addItems(userId, ItemType.Gold, 42L)

        val items = inventoryService.getItems(userId)

        assertEquals(1, items.size)
        assertEquals(ItemType.Gold, items[0].itemType)
        assertEquals(42L, items[0].amount)
    }

    @Test
    fun `concurrent addItems calls accumulate correctly`() {
        val userId = UserId(900009L)
        val threadCount = 10
        val amountPerThread = 5L
        val barrier = CyclicBarrier(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        val futures = (1..threadCount).map {
            executor.submit {
                barrier.await()
                inventoryService.addItems(userId, ItemType.Gold, amountPerThread)
            }
        }

        futures.forEach { it.get() }
        executor.shutdown()

        assertEquals(threadCount * amountPerThread, inventoryService.getItem(userId, ItemType.Gold).amount)
    }

    @Test
    fun `concurrent removeItems calls do not over-remove`() {
        val userId = UserId(900010L)
        inventoryService.addItems(userId, ItemType.Gold, 1L)

        val threadCount = 5
        val barrier = CyclicBarrier(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        val futures = (1..threadCount).map {
            executor.submit<RemoveItemsResult> {
                barrier.await()
                inventoryService.removeItems(userId, ItemType.Gold, 1L)
            }
        }

        val results = futures.map { it.get() }
        executor.shutdown()

        val successCount = results.count { it == RemoveItemsResult.Success }
        assertEquals(1, successCount, "Expected exactly one successful removal but got $successCount")
        assertEquals(0L, inventoryService.getItem(userId, ItemType.Gold).amount)
    }
}
