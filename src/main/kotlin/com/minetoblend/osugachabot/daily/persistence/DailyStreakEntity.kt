package com.minetoblend.osugachabot.daily.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "daily_streaks")
class DailyStreakEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "current_streak", nullable = false)
    var currentStreak: Int,

    @Column(name = "last_claimed_at", nullable = false)
    var lastClaimedAt: Instant,
)
