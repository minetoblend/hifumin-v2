package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class CardServiceImplTest {

    @Autowired
    private lateinit var cardService: CardService

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Test
    fun `findById returns card when it exists`() {
        val entity = cardRepository.save(CardEntity(123L, "TestUser", "US", "Rhythm Incarnate", 100, 42))

        val card = cardService.findById(CardId(entity.id))

        assertNotNull(card)
        assertEquals(CardId(entity.id), card.id)
        assertEquals("TestUser", card.username)
        assertEquals("US", card.countryCode)
        assertEquals("Rhythm Incarnate", card.title)
        assertEquals(100, card.followerCount)
        assertEquals(42, card.globalRank)
        assertEquals(CardRarity.Common, card.rarity)
    }

    @Test
    fun `findById maps rarity from entity`() {
        val entity = cardRepository.save(CardEntity(789L, "FamousPlayer", "JP", null, 200_000, null, CardRarity.Legendary))

        val card = cardService.findById(CardId(entity.id))

        assertNotNull(card)
        assertEquals(CardRarity.Legendary, card.rarity)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val card = cardService.findById(CardId(Long.MAX_VALUE))

        assertNull(card)
    }

    @Test
    fun `findById maps null fields correctly`() {
        val entity = cardRepository.save(CardEntity(456L, "NoRankUser", "DE", null, 0, null))

        val card = cardService.findById(CardId(entity.id))

        assertNotNull(card)
        assertNull(card.title)
        assertNull(card.globalRank)
    }

    @Test
    fun `getRandomCards returns requested count`() {
        repeat(5) { i -> cardRepository.save(CardEntity(i.toLong(), "RandPlayer$i", "JP", null, i, i + 1)) }

        val cards = cardService.getRandomCards(3)

        assertEquals(3, cards.size)
    }

    @Test
    fun `getRandomCards returns valid Card domain objects`() {
        repeat(3) { i -> cardRepository.save(CardEntity(100L + i, "ValidPlayer$i", "KR", null, i * 10, i + 1)) }

        val cards = cardService.getRandomCards(2)

        assertTrue(cards.all { it.id.value > 0 })
        assertTrue(cards.all { it.username.isNotBlank() })
    }
}
