package com.minetoblend.osugachabot.trading

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
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
class TradeServiceImplTest {

    @Autowired
    private lateinit var tradeService: TradeService

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var cardReplicaRepository: CardReplicaRepository

    private val userA = UserId(100L)
    private val userB = UserId(200L)

    private fun createCard(osuUserId: Long, username: String): CardEntity =
        cardRepository.save(CardEntity(osuUserId, username, "US", null, 1000, 50))

    private fun createReplica(card: CardEntity, ownerId: Long): CardReplicaEntity =
        cardReplicaRepository.save(CardReplicaEntity(card, ownerId, CardCondition.Mint))

    @Test
    fun `createTrade succeeds when both cards exist and are owned correctly`() {
        val cardA = createCard(1L, "PlayerA")
        val cardB = createCard(2L, "PlayerB")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userB.value)

        val result = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        )

        val created = assertIs<CreateTradeResult.Created>(result)
        assertEquals(TradeStatus.Pending, created.trade.status)
        assertEquals(userA, created.trade.initiatorUserId)
        assertEquals(userB, created.trade.targetUserId)
    }

    @Test
    fun `createTrade fails when trading with self`() {
        val card = createCard(3L, "SelfTrader")
        val replicaA = createReplica(card, userA.value)
        val replicaB = createReplica(card, userA.value)

        val result = tradeService.createTrade(
            userA, userA,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        )

        assertIs<CreateTradeResult.CannotTradeWithSelf>(result)
    }

    @Test
    fun `createTrade fails when offered card does not exist`() {
        val card = createCard(4L, "PlayerC")
        val replica = createReplica(card, userB.value)

        val result = tradeService.createTrade(
            userA, userB,
            CardReplicaId(Long.MAX_VALUE), CardReplicaId(replica.id)
        )

        assertIs<CreateTradeResult.OfferedCardNotFound>(result)
    }

    @Test
    fun `createTrade fails when offered card is not owned by initiator`() {
        val cardA = createCard(5L, "PlayerD")
        val cardB = createCard(6L, "PlayerE")
        val replicaA = createReplica(cardA, userB.value) // owned by B, not A
        val replicaB = createReplica(cardB, userB.value)

        val result = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        )

        assertIs<CreateTradeResult.OfferedCardNotOwned>(result)
    }

    @Test
    fun `createTrade fails when requested card does not exist`() {
        val card = createCard(7L, "PlayerF")
        val replica = createReplica(card, userA.value)

        val result = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replica.id), CardReplicaId(Long.MAX_VALUE)
        )

        assertIs<CreateTradeResult.RequestedCardNotFound>(result)
    }

    @Test
    fun `createTrade fails when requested card is not owned by target`() {
        val cardA = createCard(8L, "PlayerG")
        val cardB = createCard(9L, "PlayerH")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userA.value) // owned by A, not B

        val result = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        )

        assertIs<CreateTradeResult.RequestedCardNotOwned>(result)
    }

    @Test
    fun `acceptTrade swaps card ownership`() {
        val cardA = createCard(10L, "TraderA")
        val cardB = createCard(11L, "TraderB")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userB.value)

        val created = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        ) as CreateTradeResult.Created

        val result = tradeService.acceptTrade(created.trade.id, userB)

        val accepted = assertIs<AcceptTradeResult.Accepted>(result)
        assertEquals(TradeStatus.Accepted, accepted.trade.status)

        // Verify ownership swapped
        val updatedA = cardReplicaRepository.findById(replicaA.id).get()
        val updatedB = cardReplicaRepository.findById(replicaB.id).get()
        assertEquals(userB.value, updatedA.userId)
        assertEquals(userA.value, updatedB.userId)
    }

    @Test
    fun `acceptTrade fails when user is not the target`() {
        val cardA = createCard(12L, "TraderC")
        val cardB = createCard(13L, "TraderD")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userB.value)

        val created = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        ) as CreateTradeResult.Created

        val result = tradeService.acceptTrade(created.trade.id, userA) // initiator tries to accept

        assertIs<AcceptTradeResult.NotTargetUser>(result)
    }

    @Test
    fun `acceptTrade fails when trade is already declined`() {
        val cardA = createCard(14L, "TraderE")
        val cardB = createCard(15L, "TraderF")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userB.value)

        val created = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        ) as CreateTradeResult.Created

        tradeService.declineTrade(created.trade.id, userB)
        val result = tradeService.acceptTrade(created.trade.id, userB)

        assertIs<AcceptTradeResult.TradeNoLongerValid>(result)
    }

    @Test
    fun `acceptTrade fails when offered card was burned`() {
        val cardA = createCard(16L, "TraderG")
        val cardB = createCard(17L, "TraderH")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userB.value)

        val created = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        ) as CreateTradeResult.Created

        // Simulate card being burned/deleted
        cardReplicaRepository.deleteById(replicaA.id)

        val result = tradeService.acceptTrade(created.trade.id, userB)

        assertIs<AcceptTradeResult.CardNoLongerAvailable>(result)
    }

    @Test
    fun `declineTrade succeeds for target user`() {
        val cardA = createCard(18L, "TraderI")
        val cardB = createCard(19L, "TraderJ")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userB.value)

        val created = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        ) as CreateTradeResult.Created

        val result = tradeService.declineTrade(created.trade.id, userB)

        val declined = assertIs<DeclineTradeResult.Declined>(result)
        assertEquals(TradeStatus.Declined, declined.trade.status)
    }

    @Test
    fun `cancelTrade succeeds for initiator`() {
        val cardA = createCard(20L, "TraderK")
        val cardB = createCard(21L, "TraderL")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userB.value)

        val created = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        ) as CreateTradeResult.Created

        val result = tradeService.cancelTrade(created.trade.id, userA)

        val cancelled = assertIs<CancelTradeResult.Cancelled>(result)
        assertEquals(TradeStatus.Cancelled, cancelled.trade.status)
    }

    @Test
    fun `cancelTrade fails for non-initiator`() {
        val cardA = createCard(22L, "TraderM")
        val cardB = createCard(23L, "TraderN")
        val replicaA = createReplica(cardA, userA.value)
        val replicaB = createReplica(cardB, userB.value)

        val created = tradeService.createTrade(
            userA, userB,
            CardReplicaId(replicaA.id), CardReplicaId(replicaB.id)
        ) as CreateTradeResult.Created

        val result = tradeService.cancelTrade(created.trade.id, userB) // target tries to cancel

        assertIs<CancelTradeResult.NotInitiator>(result)
    }
}
