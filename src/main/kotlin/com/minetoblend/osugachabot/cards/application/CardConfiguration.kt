package com.minetoblend.osugachabot.cards.application

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.random.Random

@Configuration
class CardConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun random(): Random = Random.Default
}
