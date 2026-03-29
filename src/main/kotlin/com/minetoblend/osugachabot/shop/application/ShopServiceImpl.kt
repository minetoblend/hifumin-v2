package com.minetoblend.osugachabot.shop.application

import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.RemoveItemsResult
import com.minetoblend.osugachabot.shop.BuyShopItemResult
import com.minetoblend.osugachabot.shop.ShopItem
import com.minetoblend.osugachabot.shop.ShopItemId
import com.minetoblend.osugachabot.shop.ShopService
import com.minetoblend.osugachabot.users.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShopServiceImpl(
    private val inventoryService: InventoryService
) : ShopService {
    override fun getShopItems(): List<ShopItem> = listOf(
        ShopItem(
            id = ShopItemId("free claim"),
            item = FreeClaim,
            goldPrice = 100,
        ),
        ShopItem(
            id = ShopItemId("drop speedup"),
            item = DropSpeedup,
            goldPrice = 600
        ),
        ShopItem(
            id = ShopItemId("superdrop"),
            item = SuperDrop,
            goldPrice = 1500
        ),
    )

    @Transactional
    override fun buyShopItem(userId: UserId, itemId: ShopItemId, amount: Long): BuyShopItemResult {
        require(amount > 0) { "amount must be greater than 0" }

        val item = getShopItems().find { it.id == itemId }
            ?: return ItemNotFound

        val price = item.goldPrice * amount

        return when (inventoryService.removeItems(userId, Gold, price)) {
            InsufficientItems -> InsufficientFunds
            Success -> {
                inventoryService.addItems(userId, item.item, amount)
                BuyShopItemResult.Success(item)
            }
        }
    }
}