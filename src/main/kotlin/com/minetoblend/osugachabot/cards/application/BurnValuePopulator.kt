package com.minetoblend.osugachabot.cards.application

import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.persistence.CardReplicaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Component
class BurnValuePopulator(
    private val cardReplicaRepository: CardReplicaRepository,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(BurnValuePopulator::class.java)

    override fun run(args: ApplicationArguments) {
        val pageable = PageRequest.of(0, BATCH_SIZE)
        var total = 0

        while (true) {
            val batch = cardReplicaRepository.findWithStaleBurnValue(CURRENT_BURN_VALUE_VERSION, pageable)
            if (batch.isEmpty()) break

            for (replica in batch) {
                replica.burnValue = computeBurnValue(replica.card.followerCount, replica.condition, replica.foil)
                replica.burnValueVersion = CURRENT_BURN_VALUE_VERSION
            }

            cardReplicaRepository.saveAll(batch)
            total += batch.size
            logger.info("Recalculated burn_value for {}/{} card replicas (version {})", batch.size, total, CURRENT_BURN_VALUE_VERSION)
        }
    }

    companion object {
        const val CURRENT_BURN_VALUE_VERSION = 1
        private const val BATCH_SIZE = 500
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
