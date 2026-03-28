package com.minetoblend.osugachabot.cards

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class CardSeeder(
    private val cardRepository: CardRepository,
    private val objectMapper: ObjectMapper,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(CardSeeder::class.java)

    override fun run(args: ApplicationArguments) {
        val resource = javaClass.getResource("/cards.json") ?: return

        if (cardRepository.count() > 0) {
            logger.info("Card table is not empty, skipping seeding")
            return
        }

        val cards = objectMapper.readValue(resource.readText(), Array<SeededCard>::class.java)

        logger.info("Seeding card table with {} cards", cards.size)

        cardRepository.saveAllAndFlush(
            cards.map {
                CardEntity(
                    username = it.username,
                    countryCode = it.countryCode,
                    title = it.title,
                    followerCount = it.followerCount,
                    globalRank = it.rank
                )
            }
        )
    }

    private data class SeededCard(
        val id: Long,
        val countryCode: String,
        val username: String,
        val title: String?,
        val followerCount: Int,
        val rank: Int?
    )
}
