package com.minetoblend.osugachabot.cards.application

import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Component
class BurnValuePopulator(
    private val cardReplicaRepository: CardReplicaRepository,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(BurnValuePopulator::class.java)

    override fun run(args: ApplicationArguments) {
        val replicas = cardReplicaRepository.findByBurnValueIsNull()

        if (replicas.isEmpty()) return

        logger.info("Populating burn_value for {} card replicas", replicas.size)

        for (replica in replicas) {
            replica.burnValue = computeBurnValue(replica.card.followerCount, replica.condition, replica.foil)
        }

        cardReplicaRepository.saveAll(replicas)
    }
}

fun computeBurnValue(followerCount: Int, condition: CardCondition, foil: Boolean = false): Int {
    val multiplier = when (condition) {
        CardCondition.Mint -> 1f
        CardCondition.Good -> 0.5f
        CardCondition.Poor -> 0.2f
        CardCondition.Damaged -> 0.1f
    }

    val foilMultiplier = if (foil) 2f else 1f

    val baseValue = 3 * sqrt(followerCount.toFloat())
    val cardValue = (50 + baseValue) * multiplier * foilMultiplier

    return cardValue.roundToInt().coerceAtLeast(1)
}
