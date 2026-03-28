package com.minetoblend.osugachabot.cards

import org.springframework.data.jpa.repository.JpaRepository

interface CardRepository : JpaRepository<CardEntity, Long>