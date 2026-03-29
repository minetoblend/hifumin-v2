package com.minetoblend.osugachabot.drops

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.drops.persistence.DropRepository
import com.minetoblend.osugachabot.users.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class DropServiceImplTest {

    @Autowired
    private lateinit var dropService: DropService

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var dropRepository: DropRepository

    @Autowired
    private lateinit var inventoryService: com.minetoblend.osugachabot.inventory.InventoryService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private fun seedCards(count: Int = 5) {
        repeat(count) { i -> cardRepository.save(CardEntity(i.toLong(), "Player$i", "US", null, i * 10, i + 1)) }
    }

    private fun createDrop(userId: UserId = UserId(1L)): Drop {
        seedCards()
        jdbcTemplate.update("DELETE FROM dropped_cards")
        jdbcTemplate.update("DELETE FROM drops")
        jdbcTemplate.update("DELETE FROM cooldowns")
        jdbcTemplate.update("DELETE FROM inventory_items")
        return (dropService.createDrop(userId) as CreateDropResult.Created).drop
    }

    @Test
    fun `createDrop returns a drop with 3 cards`() {
        seedCards()
        jdbcTemplate.update("DELETE FROM dropped_cards")
        jdbcTemplate.update("DELETE FROM drops")
        jdbcTemplate.update("DELETE FROM cooldowns")

        val result = dropService.createDrop(UserId(1L))

        assertIs<CreateDropResult.Created>(result)
        assertEquals(3, result.drop.cards.size)
        assertTrue(result.drop.id.value > 0)
    }

    @Test
    fun `createDrop persists the drop`() {
        seedCards()
        jdbcTemplate.update("DELETE FROM dropped_cards")
        jdbcTemplate.update("DELETE FROM drops")
        jdbcTemplate.update("DELETE FROM cooldowns")

        val result = dropService.createDrop(UserId(1L))

        assertIs<CreateDropResult.Created>(result)
        assertNotNull(dropRepository.findById(result.drop.id.value).orElse(null))
    }

    @Test
    fun `createDrop cards have sequential indices`() {
        seedCards()
        jdbcTemplate.update("DELETE FROM dropped_cards")
        jdbcTemplate.update("DELETE FROM drops")
        jdbcTemplate.update("DELETE FROM cooldowns")

        val result = dropService.createDrop(UserId(1L))

        assertIs<CreateDropResult.Created>(result)
        assertEquals(listOf(0, 1, 2), result.drop.cards.map { it.index })
    }

    @Test
    fun `createDrop cards are not claimed`() {
        seedCards()
        jdbcTemplate.update("DELETE FROM dropped_cards")
        jdbcTemplate.update("DELETE FROM drops")
        jdbcTemplate.update("DELETE FROM cooldowns")

        val result = dropService.createDrop(UserId(1L))

        assertIs<CreateDropResult.Created>(result)
        result.drop.cards.forEach { assertNull(it.claimedBy) }
    }

    @Test
    fun `createDrop returns OnCooldown when same user calls again within 10 minutes`() {
        seedCards()
        jdbcTemplate.update("DELETE FROM dropped_cards")
        jdbcTemplate.update("DELETE FROM drops")
        jdbcTemplate.update("DELETE FROM cooldowns")
        val userId = UserId(1L)

        dropService.createDrop(userId)
        val result = dropService.createDrop(userId)

        assertIs<CreateDropResult.OnCooldown>(result)
        assertTrue(result.remaining.isPositive())
    }

    @Test
    fun `createDrop returns Created after cooldown has elapsed`() {
        seedCards()
        jdbcTemplate.update("DELETE FROM dropped_cards")
        jdbcTemplate.update("DELETE FROM drops")
        jdbcTemplate.update("DELETE FROM cooldowns")
        val userId = UserId(1L)

        dropService.createDrop(userId)
        jdbcTemplate.update(
            "UPDATE cooldowns SET last_used_at = ? WHERE type = 'drop'",
            Timestamp.from(Instant.now().minusSeconds(601)),
        )

        val result = dropService.createDrop(userId)

        assertIs<CreateDropResult.Created>(result)
    }

    @Test
    fun `createDrop allows different user to drop during another user's cooldown`() {
        seedCards()
        jdbcTemplate.update("DELETE FROM dropped_cards")
        jdbcTemplate.update("DELETE FROM drops")
        jdbcTemplate.update("DELETE FROM cooldowns")

        dropService.createDrop(UserId(1L))
        val result = dropService.createDrop(UserId(2L))

        assertIs<CreateDropResult.Created>(result)
    }

    @Test
    fun `claimCard marks the card as claimed by the user`() {
        val drop = createDrop()
        val userId = UserId(42L)

        val result = dropService.claimCard(drop.id, 0, userId)

        assertIs<ClaimResult.Claimed>(result)
        assertEquals(userId, result.drop.cards[0].claimedBy)
    }

    @Test
    fun `claimCard returns a CardReplica for the user`() {
        val drop = createDrop()
        val userId = UserId(99L)

        val result = dropService.claimCard(drop.id, 1, userId)

        assertIs<ClaimResult.Claimed>(result)
        assertEquals(userId, result.replica.userId)
        assertEquals(drop.cards[1].card.id, result.replica.card.id)
        assertEquals(drop.cards[1].condition, result.replica.condition)
    }

    @Test
    fun `claimCard returns AlreadyClaimed when card was already taken`() {
        val drop = createDrop()
        val user1 = UserId(1L)
        val user2 = UserId(2L)

        dropService.claimCard(drop.id, 0, user1)
        val result = dropService.claimCard(drop.id, 0, user2)

        assertIs<ClaimResult.AlreadyClaimed>(result)
    }

    @Test
    fun `claimCard AlreadyClaimed still returns updated drop state`() {
        val drop = createDrop()
        val user1 = UserId(1L)
        val user2 = UserId(2L)

        dropService.claimCard(drop.id, 0, user1)
        val result = dropService.claimCard(drop.id, 0, user2)

        assertIs<ClaimResult.AlreadyClaimed>(result)
        assertEquals(user1, result.drop.cards[0].claimedBy)
    }

    @Test
    fun `claimCard returns DropNotFound for unknown drop id`() {
        jdbcTemplate.update("DELETE FROM cooldowns")

        val result = dropService.claimCard(DropId(Long.MAX_VALUE), 0, UserId(1L))

        assertIs<ClaimResult.DropNotFound>(result)
    }

    @Test
    fun `claimCard returns DropNotFound for invalid card index`() {
        val drop = createDrop()

        val result = dropService.claimCard(drop.id, 99, UserId(1L))

        assertIs<ClaimResult.DropNotFound>(result)
    }

    @Test
    fun `claimCard returns Expired when drop is older than 1 minute`() {
        val drop = createDrop()

        jdbcTemplate.update(
            "UPDATE drops SET created_at = ? WHERE id = ?",
            Timestamp.from(Instant.now().minusSeconds(61)),
            drop.id.value,
        )

        val result = dropService.claimCard(drop.id, 0, UserId(1L))

        assertIs<ClaimResult.Expired>(result)
    }

    @Test
    fun `multiple cards in the same drop can be claimed independently`() {
        val drop = createDrop()
        val user1 = UserId(1L)
        val user2 = UserId(2L)

        val result1 = dropService.claimCard(drop.id, 0, user1)
        val result2 = dropService.claimCard(drop.id, 1, user2)

        assertIs<ClaimResult.Claimed>(result1)
        assertIs<ClaimResult.Claimed>(result2)
        assertEquals(user1, result1.drop.cards[0].claimedBy)
        assertEquals(user2, result2.drop.cards[1].claimedBy)
    }

    @Test
    fun `claimCard returns OnCooldown when same user claims again within 1 minute`() {
        val drop = createDrop()
        val userId = UserId(42L)

        dropService.claimCard(drop.id, 0, userId)
        val result = dropService.claimCard(drop.id, 1, userId)

        assertIs<ClaimResult.OnCooldown>(result)
        assertTrue(result.remaining.isPositive())
    }

    @Test
    fun `claimCard allows different users to claim without cooldown interference`() {
        val drop = createDrop()

        dropService.claimCard(drop.id, 0, UserId(1L))
        val result = dropService.claimCard(drop.id, 1, UserId(2L))

        assertIs<ClaimResult.Claimed>(result)
    }

    @Test
    fun `claimCard does not consume cooldown when drop is not found`() {
        val drop = createDrop()
        val userId = UserId(42L)

        dropService.claimCard(DropId(Long.MAX_VALUE), 0, userId)
        val result = dropService.claimCard(drop.id, 0, userId)

        assertIs<ClaimResult.Claimed>(result)
    }

    @Test
    fun `claimCard does not consume cooldown when drop is expired`() {
        val drop = createDrop()
        val userId = UserId(42L)

        jdbcTemplate.update(
            "UPDATE drops SET created_at = ? WHERE id = ?",
            Timestamp.from(Instant.now().minusSeconds(61)),
            drop.id.value,
        )
        dropService.claimCard(drop.id, 0, userId)

        jdbcTemplate.update(
            "UPDATE drops SET created_at = ? WHERE id = ?",
            Timestamp.from(Instant.now()),
            drop.id.value,
        )
        val result = dropService.claimCard(drop.id, 0, userId)

        assertIs<ClaimResult.Claimed>(result)
    }

    @Test
    fun `claimCard does not consume cooldown when card is already claimed`() {
        val drop = createDrop()
        val user1 = UserId(1L)
        val user2 = UserId(2L)

        dropService.claimCard(drop.id, 0, user1)
        dropService.claimCard(drop.id, 0, user2)
        val result = dropService.claimCard(drop.id, 1, user2)

        assertIs<ClaimResult.Claimed>(result)
    }

    @Test
    fun `claimCard returns Claimed after claim cooldown has elapsed`() {
        val drop = createDrop()
        val userId = UserId(42L)

        dropService.claimCard(drop.id, 0, userId)
        jdbcTemplate.update(
            "UPDATE cooldowns SET last_used_at = ? WHERE type = 'claim' AND user_id = ?",
            Timestamp.from(Instant.now().minusSeconds(61)),
            userId.value,
        )

        val result = dropService.claimCard(drop.id, 1, userId)

        assertIs<ClaimResult.Claimed>(result)
    }

    @Test
    fun `claimCard bypasses claim cooldown when user has a FreeClaim item`() {
        val drop = createDrop()
        val userId = UserId(42L)

        dropService.claimCard(drop.id, 0, userId)
        inventoryService.addItems(userId, com.minetoblend.osugachabot.inventory.ItemType.FreeClaim, 1)
        val result = dropService.claimCard(drop.id, 1, userId)

        assertIs<ClaimResult.Claimed>(result)
    }

    @Test
    fun `claimCard consumes the FreeClaim item when used to bypass cooldown`() {
        val drop = createDrop()
        val userId = UserId(42L)

        dropService.claimCard(drop.id, 0, userId)
        inventoryService.addItems(userId, com.minetoblend.osugachabot.inventory.ItemType.FreeClaim, 1)
        dropService.claimCard(drop.id, 1, userId)

        assertEquals(0, inventoryService.getItem(userId, com.minetoblend.osugachabot.inventory.ItemType.FreeClaim).amount)
    }

    @Test
    fun `claimCard returns OnCooldown when user is on cooldown and has no FreeClaim item`() {
        val drop = createDrop()
        val userId = UserId(42L)

        dropService.claimCard(drop.id, 0, userId)
        val result = dropService.claimCard(drop.id, 1, userId)

        assertIs<ClaimResult.OnCooldown>(result)
    }

    @Test
    fun `claimCard does not consume FreeClaim item when not on cooldown`() {
        val drop = createDrop()
        val userId = UserId(42L)

        inventoryService.addItems(userId, com.minetoblend.osugachabot.inventory.ItemType.FreeClaim, 1)
        dropService.claimCard(drop.id, 0, userId)

        assertEquals(1, inventoryService.getItem(userId, com.minetoblend.osugachabot.inventory.ItemType.FreeClaim).amount)
    }
}
