package com.minetoblend.osugachabot.leaderboard.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CollectionValueRepository : JpaRepository<CollectionValueEntity, Long> {
    fun findAllByOrderByTotalValueDesc(pageable: Pageable): Page<CollectionValueEntity>
}
