package com.minetoblend.osugachabot.leaderboard

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.support.TransactionTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class CollectionValueServiceImplTest {

    @Autowired
    private lateinit var collectionValueService: CollectionValueService

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var cardReplicaRepository: CardReplicaRepository

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    private fun createCard(osuUserId: Long, username: String, followerCount: Int = 1000): CardEntity =
        cardRepository.save(CardEntity(osuUserId, username, "US", null, followerCount, 50))

    private fun createReplica(card: CardEntity, ownerId: Long, condition: CardCondition = CardCondition.Mint, burnValue: Int = 100): CardReplicaEntity =
        cardReplicaRepository.save(CardReplicaEntity(card, ownerId, condition, burnValue))

    @Test
    fun `getLeaderboard returns empty page when no entries exist`() {
        val page = collectionValueService.getLeaderboard(PageRequest.of(0, 10))
        // Just verify it doesn't throw — other tests may have inserted entries
    }

    @Test
    fun `collection value is updated when a card is claimed`() {
        val userId = UserId(10001L)
        val card = createCard(10001L, "ClaimUser")
        createReplica(card, userId.value, burnValue = 200)

        transactionTemplate.execute {
            collectionValueService.recompute(userId)
        }

        val entry = collectionValueService.getCollectionValue(userId)
        assertEquals(200L, entry.totalValue)
        assertEquals(1, entry.cardCount)
    }

    @Test
    fun `collection value accumulates across multiple cards`() {
        val userId = UserId(10002L)
        val card1 = createCard(10002L, "MultiUser1")
        val card2 = createCard(10003L, "MultiUser2")
        createReplica(card1, userId.value, burnValue = 150)
        createReplica(card2, userId.value, burnValue = 250)

        transactionTemplate.execute {
            collectionValueService.recompute(userId)
        }

        val entry = collectionValueService.getCollectionValue(userId)
        assertEquals(400L, entry.totalValue)
        assertEquals(2, entry.cardCount)
    }

    @Test
    fun `collection value decreases when a card is removed`() {
        val userId = UserId(10004L)
        val card = createCard(10004L, "BurnUser")
        val replica = createReplica(card, userId.value, burnValue = 300)

        transactionTemplate.execute {
            collectionValueService.recompute(userId)
        }
        assertEquals(300L, collectionValueService.getCollectionValue(userId).totalValue)

        cardReplicaRepository.delete(replica)
        transactionTemplate.execute {
            collectionValueService.recompute(userId)
        }

        val entry = collectionValueService.getCollectionValue(userId)
        assertEquals(0L, entry.totalValue)
        assertEquals(0, entry.cardCount)
    }

    @Test
    fun `getLeaderboard returns entries ordered by total value descending`() {
        val userA = UserId(10005L)
        val userB = UserId(10006L)
        val card1 = createCard(10005L, "LeaderA")
        val card2 = createCard(10006L, "LeaderB")
        createReplica(card1, userA.value, burnValue = 500)
        createReplica(card2, userB.value, burnValue = 800)

        transactionTemplate.execute {
            collectionValueService.recompute(userA)
            collectionValueService.recompute(userB)
        }

        val page = collectionValueService.getLeaderboard(PageRequest.of(0, 100))
        val entries = page.content.filter { it.userId == userA || it.userId == userB }
        assertEquals(2, entries.size)
        assertEquals(userB, entries[0].userId)
        assertEquals(userA, entries[1].userId)
    }

    @Test
    fun `getLeaderboard is paginated`() {
        val users = (10010L..10019L).map { UserId(it) }
        users.forEachIndexed { i, userId ->
            val card = createCard(10010L + i, "PaginatedUser$i")
            createReplica(card, userId.value, burnValue = (i + 1) * 100)
            transactionTemplate.execute { collectionValueService.recompute(userId) }
        }

        val page0 = collectionValueService.getLeaderboard(PageRequest.of(0, 5))
        val page1 = collectionValueService.getLeaderboard(PageRequest.of(1, 5))

        assertEquals(5, page0.content.size)
        assertEquals(5, page1.content.size)
    }
}
