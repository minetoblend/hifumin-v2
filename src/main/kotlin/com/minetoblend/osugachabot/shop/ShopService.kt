package com.minetoblend.osugachabot.shop

import com.minetoblend.osugachabot.users.UserId

interface ShopService {
    fun getShopItems(): List<ShopItem>

    fun buyShopItem(userId: UserId, itemId: ShopItemId, amount: Long): BuyShopItemResult
}

sealed interface BuyShopItemResult {
    data class Success(val item: ShopItem) : BuyShopItemResult
    data object ItemNotFound : BuyShopItemResult
    data object InsufficientFunds : BuyShopItemResult
}