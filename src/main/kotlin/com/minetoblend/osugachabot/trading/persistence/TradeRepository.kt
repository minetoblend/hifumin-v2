package com.minetoblend.osugachabot.trading.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TradeRepository : JpaRepository<TradeEntity, Long>
