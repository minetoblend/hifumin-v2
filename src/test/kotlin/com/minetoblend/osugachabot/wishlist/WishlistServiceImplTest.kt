package com.minetoblend.osugachabot.wishlist

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.CardId
import com.minetoblend.osugachabot.cards.CardRarity
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class WishlistServiceImplTest {

    @Autowired
    private lateinit var wishlistService: WishlistService

    @Autowired
    private lateinit var cardRepository: CardRepository

    private val userA = UserId(100L)
    private val userB = UserId(200L)

    private fun createCard(osuUserId: Long, username: String): CardEntity =
        cardRepository.save(CardEntity(osuUserId, username, "US", null, 1000, 50))

    @Test
    fun `addToWishlist succeeds for a known card`() {
        val card = createCard(1001L, "WishPlayer1")

        val result = wishlistService.addToWishlist(userA, CardId(card.id))

        assertIs<AddToWishlistResult.Added>(result)
    }

    @Test
    fun `addToWishlist fails when card does not exist`() {
        val result = wishlistService.addToWishlist(userA, CardId(Long.MAX_VALUE))

        assertIs<AddToWishlistResult.CardNotFound>(result)
    }

    @Test
    fun `addToWishlist fails when card is already wishlisted`() {
        val card = createCard(1002L, "WishPlayer2")
        wishlistService.addToWishlist(userA, CardId(card.id))

        val result = wishlistService.addToWishlist(userA, CardId(card.id))

        assertIs<AddToWishlistResult.AlreadyWishlisted>(result)
    }

    @Test
    fun `addToWishlist fails when wishlist is full`() {
        repeat(WishlistService.MAX_WISHLIST_SIZE) { i ->
            val card = createCard(2000L + i, "WishFull$i")
            wishlistService.addToWishlist(userB, CardId(card.id))
        }
        val extraCard = createCard(3000L, "WishExtra")

        val result = wishlistService.addToWishlist(userB, CardId(extraCard.id))

        assertIs<AddToWishlistResult.WishlistFull>(result)
    }

    @Test
    fun `removeFromWishlist succeeds when card is wishlisted`() {
        val card = createCard(1003L, "WishPlayer3")
        wishlistService.addToWishlist(userA, CardId(card.id))

        val result = wishlistService.removeFromWishlist(userA, CardId(card.id))

        assertIs<RemoveFromWishlistResult.Removed>(result)
        assertEquals(0, wishlistService.getWishlist(userA).count { it.cardId == CardId(card.id) })
    }

    @Test
    fun `removeFromWishlist fails when card is not wishlisted`() {
        val card = createCard(1004L, "WishPlayer4")

        val result = wishlistService.removeFromWishlist(userA, CardId(card.id))

        assertIs<RemoveFromWishlistResult.NotWishlisted>(result)
    }

    @Test
    fun `getWishlist returns all entries for user`() {
        val card1 = createCard(1005L, "WishPlayer5")
        val card2 = createCard(1006L, "WishPlayer6")
        val userId = UserId(300L)
        wishlistService.addToWishlist(userId, CardId(card1.id))
        wishlistService.addToWishlist(userId, CardId(card2.id))

        val wishlist = wishlistService.getWishlist(userId)

        assertEquals(2, wishlist.size)
    }

    @Test
    fun `getWishlistedUserIdsForCards returns correct mapping`() {
        val card1 = createCard(1007L, "WishPlayer7")
        val card2 = createCard(1008L, "WishPlayer8")
        val userId1 = UserId(400L)
        val userId2 = UserId(500L)
        wishlistService.addToWishlist(userId1, CardId(card1.id))
        wishlistService.addToWishlist(userId2, CardId(card1.id))
        wishlistService.addToWishlist(userId1, CardId(card2.id))

        val result = wishlistService.getWishlistedUserIdsForCards(listOf(CardId(card1.id), CardId(card2.id)))

        assertEquals(2, result[CardId(card1.id)]?.size)
        assertEquals(1, result[CardId(card2.id)]?.size)
    }
}
