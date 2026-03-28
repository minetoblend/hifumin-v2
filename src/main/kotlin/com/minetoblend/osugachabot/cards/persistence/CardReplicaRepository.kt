package com.minetoblend.osugachabot.cards.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CardReplicaRepository : JpaRepository<CardReplicaEntity, Long>