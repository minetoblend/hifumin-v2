package com.minetoblend.osugachabot.users.settings.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user_settings")
class UserSettingsEntity(
    @Id
    val userId: Long,
    @Column(nullable = false)
    var reminders: Boolean = false,
)
