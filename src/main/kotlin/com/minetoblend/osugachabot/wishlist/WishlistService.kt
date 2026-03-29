package com.minetoblend.osugachabot.wishlist

import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.users.UserId

interface WishlistService {
    fun addToWishlist(userId: UserId, cardId: CardId): AddToWishlistResult
    fun removeFromWishlist(userId: UserId, cardId: CardId): RemoveFromWishlistResult
    fun getWishlist(userId: UserId): List<WishlistEntry>
    fun getWishlistedUserIdsForCards(cardIds: List<CardId>): Map<CardId, List<UserId>>

    companion object {
        const val MAX_WISHLIST_SIZE = 10
    }
}

sealed interface AddToWishlistResult {
    data object Added : AddToWishlistResult
    data object AlreadyWishlisted : AddToWishlistResult
    data object CardNotFound : AddToWishlistResult
    data object WishlistFull : AddToWishlistResult
}

sealed interface RemoveFromWishlistResult {
    data object Removed : RemoveFromWishlistResult
    data object NotWishlisted : RemoveFromWishlistResult
}
