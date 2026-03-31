package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.application.BurnValuePopulator
import com.minetoblend.osugachabot.cards.application.computeBurnValue
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.Test

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class BurnValuePopulatorTest {

    @Autowired
    private lateinit var burnValuePopulator: BurnValuePopulator

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var cardReplicaRepository: CardReplicaRepository

    @Autowired
    private lateinit var noopArgs: ApplicationArguments

    @Test
    fun `replicas with null burn value get populated with current version`() {
        val card = cardRepository.save(CardEntity(9901L, "PopulatorUser1", "JP", null, 10000, 1))
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, 1L, CardCondition.Mint, burnValue = null))

        burnValuePopulator.run(noopArgs)

        val updated = cardReplicaRepository.findById(entity.id).get()
        assertNotNull(updated.burnValue)
        assertEquals(computeBurnValue(10000, CardCondition.Mint), updated.burnValue)
        assertEquals(BurnValuePopulator.CURRENT_BURN_VALUE_VERSION, updated.burnValueVersion)
    }

    @Test
    fun `replicas with stale version get recalculated with current version`() {
        val card = cardRepository.save(CardEntity(9902L, "PopulatorUser2", "KR", null, 5000, 1))
        val entity = cardReplicaRepository.save(
            CardReplicaEntity(card, 2L, CardCondition.Good, burnValue = 999, burnValueVersion = 0)
        )

        burnValuePopulator.run(noopArgs)

        val updated = cardReplicaRepository.findById(entity.id).get()
        assertEquals(computeBurnValue(5000, CardCondition.Good), updated.burnValue)
        assertEquals(BurnValuePopulator.CURRENT_BURN_VALUE_VERSION, updated.burnValueVersion)
    }

    @Test
    fun `replicas already at current version are not modified`() {
        val card = cardRepository.save(CardEntity(9903L, "PopulatorUser3", "US", null, 2000, 1))
        val expectedBurnValue = computeBurnValue(2000, CardCondition.Poor)
        val entity = cardReplicaRepository.save(
            CardReplicaEntity(
                card, 3L, CardCondition.Poor,
                burnValue = expectedBurnValue,
                burnValueVersion = BurnValuePopulator.CURRENT_BURN_VALUE_VERSION,
            )
        )

        burnValuePopulator.run(noopArgs)

        val after = cardReplicaRepository.findById(entity.id).get()
        assertEquals(expectedBurnValue, after.burnValue)
        assertEquals(BurnValuePopulator.CURRENT_BURN_VALUE_VERSION, after.burnValueVersion)
    }
}
