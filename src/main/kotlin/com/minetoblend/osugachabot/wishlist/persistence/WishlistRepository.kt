package com.minetoblend.osugachabot.wishlist.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface WishlistRepository : JpaRepository<WishlistEntity, Long> {
    fun findByUserId(userId: Long): List<WishlistEntity>
    fun findByUserIdAndCardId(userId: Long, cardId: Long): WishlistEntity?
    fun countByUserId(userId: Long): Int
    fun findByCardIdIn(cardIds: List<Long>): List<WishlistEntity>
    fun deleteByUserIdAndCardId(userId: Long, cardId: Long): Int
}
