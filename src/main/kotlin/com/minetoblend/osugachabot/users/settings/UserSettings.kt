package com.minetoblend.osugachabot.users.settings

import com.minetoblend.osugachabot.users.UserId

data class UserSettings(
    val reminders: Boolean,
) {
    companion object {
        val Default = UserSettings(reminders = false)
    }
}
