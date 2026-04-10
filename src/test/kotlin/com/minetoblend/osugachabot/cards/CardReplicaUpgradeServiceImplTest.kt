package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.inventory.InventoryService
import com.minetoblend.osugachabot.inventory.ItemType
import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.toUserId
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class CardReplicaUpgradeServiceImplTest {

    @Autowired
    private lateinit var cardReplicaService: CardReplicaService

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var cardReplicaRepository: CardReplicaRepository

    @Autowired
    private lateinit var inventoryService: InventoryService

    @Autowired
    private lateinit var upgradePityService: UpgradePityService

    @MockitoBean
    private lateinit var random: Random

    private fun saveCard(): CardEntity =
        cardRepository.save(CardEntity(0L, "UpgradeUser", "AU", null, 50, 100))

    @Test
    fun `upgradeCard returns NotFound for unknown id`() {
        val result = cardReplicaService.upgradeCard(CardReplicaId(Long.MAX_VALUE), UserId(1L))

        assertEquals(UpgradeCardResult.NotFound, result)
    }

    @Test
    fun `upgradeCard returns NotOwned when user does not own the card`() {
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, 10L, CardCondition.Poor))

        val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), UserId(99L))

        assertEquals(UpgradeCardResult.NotOwned, result)
    }

    @Test
    fun `upgradeCard returns AlreadyMint when card is already mint`() {
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, 20L, CardCondition.Mint))

        val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), 20L.toUserId())

        assertEquals(UpgradeCardResult.AlreadyMint, result)
    }

    @Test
    fun `upgradeCard returns InsufficientGold when user lacks gold`() {
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, 30L, CardCondition.Good))

        val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), 30L.toUserId())

        assertEquals(UpgradeCardResult.InsufficientGold, result)
    }

    @Test
    fun `upgradeCard deducts gold and upgrades condition on success`() {
        val userId = UserId(40L)
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Damaged))
        inventoryService.addItems(userId, ItemType.Gold, 500L)

        given(random.nextDouble()).willReturn(0.0) // always succeed

        val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)

        assertIs<UpgradeCardResult.Success>(result)
        assertEquals(CardCondition.Poor, result.newCondition)

        val upgraded = cardReplicaRepository.findById(entity.id).get()
        assertEquals(CardCondition.Poor, upgraded.condition)

        val goldAfter = inventoryService.getItem(userId, ItemType.Gold).amount
        assertEquals(500L - CardCondition.Damaged.upgradeCost, goldAfter)
    }

    @Test
    fun `upgradeCard deducts gold but does not change condition on failure`() {
        val userId = UserId(50L)
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Poor))
        inventoryService.addItems(userId, ItemType.Gold, 500L)

        given(random.nextDouble()).willReturn(1.0) // always fail

        val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)

        assertIs<UpgradeCardResult.Failed>(result)
        assertEquals(CardCondition.Poor, result.condition)

        val unchanged = cardReplicaRepository.findById(entity.id).get()
        assertEquals(CardCondition.Poor, unchanged.condition)

        val goldAfter = inventoryService.getItem(userId, ItemType.Gold).amount
        assertEquals(500L - CardCondition.Poor.upgradeCost, goldAfter)
    }

    @Test
    fun `upgradeCard upgrades Poor to Good on success`() {
        val userId = UserId(60L)
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Poor))
        inventoryService.addItems(userId, ItemType.Gold, 1000L)

        given(random.nextDouble()).willReturn(0.0)

        val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)

        assertIs<UpgradeCardResult.Success>(result)
        assertEquals(CardCondition.Good, result.newCondition)
    }

    @Test
    fun `upgradeCard increments pity on failure`() {
        val userId = UserId(80L)
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Good))
        inventoryService.addItems(userId, ItemType.Gold, 10000L)

        given(random.nextDouble()).willReturn(1.0) // always fail

        cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)
        cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)

        assertEquals(2, upgradePityService.getPity(userId, CardCondition.Good))
    }

    @Test
    fun `upgradeCard resets pity on natural success`() {
        val userId = UserId(81L)
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Good))
        inventoryService.addItems(userId, ItemType.Gold, 10000L)

        given(random.nextDouble()).willReturn(1.0) // fail
        cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)
        cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)
        assertEquals(2, upgradePityService.getPity(userId, CardCondition.Good))

        given(random.nextDouble()).willReturn(0.0) // succeed
        cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)

        assertEquals(0, upgradePityService.getPity(userId, CardCondition.Good))
    }

    @Test
    fun `upgradeCard guarantees success once pity threshold is reached`() {
        val userId = UserId(82L)
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Good))
        inventoryService.addItems(userId, ItemType.Gold, 100000L)

        given(random.nextDouble()).willReturn(1.0) // rng would always fail

        val threshold = CardCondition.Good.upgradePityThreshold
        repeat(threshold - 1) {
            val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)
            assertIs<UpgradeCardResult.Failed>(result)
        }

        // Next attempt should be guaranteed even with unfavorable rng
        val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)

        assertIs<UpgradeCardResult.Success>(result)
        assertEquals(CardCondition.Mint, result.newCondition)
        assertEquals(0, upgradePityService.getPity(userId, CardCondition.Good))
    }

    @Test
    fun `pity is tracked per source condition`() {
        val userId = UserId(83L)
        val card = saveCard()
        val poorEntity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Poor))
        val goodEntity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Good))
        inventoryService.addItems(userId, ItemType.Gold, 10000L)

        given(random.nextDouble()).willReturn(1.0)

        cardReplicaService.upgradeCard(CardReplicaId(poorEntity.id), userId)
        cardReplicaService.upgradeCard(CardReplicaId(goodEntity.id), userId)
        cardReplicaService.upgradeCard(CardReplicaId(goodEntity.id), userId)

        assertEquals(1, upgradePityService.getPity(userId, CardCondition.Poor))
        assertEquals(2, upgradePityService.getPity(userId, CardCondition.Good))
    }

    @Test
    fun `pity is tracked per user`() {
        val cardA = saveCard()
        val cardB = saveCard()
        val userA = UserId(84L)
        val userB = UserId(85L)
        val entityA = cardReplicaRepository.save(CardReplicaEntity(cardA, userA.value, CardCondition.Good))
        val entityB = cardReplicaRepository.save(CardReplicaEntity(cardB, userB.value, CardCondition.Good))
        inventoryService.addItems(userA, ItemType.Gold, 10000L)
        inventoryService.addItems(userB, ItemType.Gold, 10000L)

        given(random.nextDouble()).willReturn(1.0)

        cardReplicaService.upgradeCard(CardReplicaId(entityA.id), userA)

        assertEquals(1, upgradePityService.getPity(userA, CardCondition.Good))
        assertEquals(0, upgradePityService.getPity(userB, CardCondition.Good))
    }

    @Test
    fun `upgradeCard upgrades Good to Mint on success`() {
        val userId = UserId(70L)
        val card = saveCard()
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, userId.value, CardCondition.Good))
        inventoryService.addItems(userId, ItemType.Gold, 2000L)

        given(random.nextDouble()).willReturn(0.0)

        val result = cardReplicaService.upgradeCard(CardReplicaId(entity.id), userId)

        assertIs<UpgradeCardResult.Success>(result)
        assertEquals(CardCondition.Mint, result.newCondition)
    }
}
