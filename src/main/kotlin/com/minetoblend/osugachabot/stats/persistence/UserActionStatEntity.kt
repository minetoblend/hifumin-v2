package com.minetoblend.osugachabot.stats.persistence

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable

@Embeddable
data class UserActionStatId(
    var userId: Long = 0,
    var action: String = "",
) : Serializable

@Entity
@Table(name = "user_action_stats")
class UserActionStatEntity(
    @EmbeddedId
    var id: UserActionStatId,
    var count: Long = 0,
)
