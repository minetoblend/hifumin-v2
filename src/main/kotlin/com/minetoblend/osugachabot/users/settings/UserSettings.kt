package com.minetoblend.osugachabot.users.settings

import com.minetoblend.osugachabot.users.UserId

data class UserSettings(
    val userId: UserId,
    val reminders: Boolean,
)
