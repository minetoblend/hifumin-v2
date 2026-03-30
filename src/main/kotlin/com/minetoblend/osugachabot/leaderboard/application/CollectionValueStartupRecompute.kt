package com.minetoblend.osugachabot.leaderboard.application

import com.minetoblend.osugachabot.leaderboard.CollectionValueService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class CollectionValueStartupRecompute(
    private val collectionValueService: CollectionValueService,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(CollectionValueStartupRecompute::class.java)

    override fun run(args: ApplicationArguments) {
        log.info("Starting collection value recompute on startup")
        collectionValueService.recomputeAll()
    }
}
