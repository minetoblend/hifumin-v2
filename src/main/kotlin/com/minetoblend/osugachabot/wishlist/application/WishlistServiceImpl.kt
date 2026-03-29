package com.minetoblend.osugachabot.wishlist.application

import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.wishlist.AddToWishlistResult
import com.minetoblend.osugachabot.wishlist.RemoveFromWishlistResult
import com.minetoblend.osugachabot.wishlist.WishlistEntry
import com.minetoblend.osugachabot.wishlist.WishlistId
import com.minetoblend.osugachabot.wishlist.WishlistService
import com.minetoblend.osugachabot.wishlist.persistence.WishlistEntity
import com.minetoblend.osugachabot.wishlist.persistence.WishlistRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WishlistServiceImpl(
    private val wishlistRepository: WishlistRepository,
    private val cardRepository: CardRepository,
) : WishlistService {

    @Transactional
    override fun addToWishlist(userId: UserId, cardId: CardId): AddToWishlistResult {
        val card = cardRepository.findByIdOrNull(cardId.value) ?: return AddToWishlistResult.CardNotFound

        if (wishlistRepository.findByUserIdAndCardId(userId.value, cardId.value) != null)
            return AddToWishlistResult.AlreadyWishlisted

        if (wishlistRepository.countByUserId(userId.value) >= WishlistService.MAX_WISHLIST_SIZE)
            return AddToWishlistResult.WishlistFull

        wishlistRepository.save(WishlistEntity(userId.value, card))
        return AddToWishlistResult.Added
    }

    @Transactional
    override fun removeFromWishlist(userId: UserId, cardId: CardId): RemoveFromWishlistResult {
        val deleted = wishlistRepository.deleteByUserIdAndCardId(userId.value, cardId.value)
        return if (deleted > 0) RemoveFromWishlistResult.Removed else RemoveFromWishlistResult.NotWishlisted
    }

    override fun getWishlist(userId: UserId): List<WishlistEntry> =
        wishlistRepository.findByUserId(userId.value).map { it.toDomain() }

    override fun getWishlistedUserIdsForCards(cardIds: List<CardId>): Map<CardId, List<UserId>> {
        if (cardIds.isEmpty()) return emptyMap()
        return wishlistRepository.findByCardIdIn(cardIds.map { it.value })
            .groupBy({ CardId(it.card.id) }, { UserId(it.userId) })
    }

    private fun WishlistEntity.toDomain() = WishlistEntry(
        id = WishlistId(id),
        userId = UserId(userId),
        cardId = CardId(card.id),
        createdAt = createdAt,
    )
}
