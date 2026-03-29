package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.TestcontainersConfiguration
import com.minetoblend.osugachabot.cards.persistence.CardEntity
import com.minetoblend.osugachabot.cards.persistence.CardRepository
import com.minetoblend.osugachabot.cards.persistence.CardReplicaEntity
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import com.minetoblend.osugachabot.users.toUserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class CardReplicaServiceImplTest {

    @Autowired
    private lateinit var cardReplicaService: CardReplicaService

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var cardReplicaRepository: CardReplicaRepository

    @Test
    fun `findById returns replica when it exists`() {
        val card = cardRepository.save(CardEntity(111L, "ReplicaUser", "AU", null, 50, 10))
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, 42L, CardCondition.Mint))

        val replica = cardReplicaService.findById(CardReplicaId(entity.id))

        assertNotNull(replica)
        assertEquals(CardReplicaId(entity.id), replica.id)
        assertEquals(42L.toUserId(), replica.userId)
        assertEquals(CardCondition.Mint, replica.condition)
        assertEquals("ReplicaUser", replica.card.username)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val replica = cardReplicaService.findById(CardReplicaId(Long.MAX_VALUE))

        assertNull(replica)
    }

    @Test
    fun `findById maps condition correctly`() {
        val card = cardRepository.save(CardEntity(222L, "DamagedUser", "BR", null, 10, 500))
        val entity = cardReplicaRepository.save(CardReplicaEntity(card, 7L, CardCondition.Damaged))

        val replica = cardReplicaService.findById(CardReplicaId(entity.id))

        assertNotNull(replica)
        assertEquals(CardCondition.Damaged, replica.condition)
    }
}
