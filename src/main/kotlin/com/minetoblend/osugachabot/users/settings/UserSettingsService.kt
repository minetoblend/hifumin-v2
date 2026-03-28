package com.minetoblend.osugachabot.users.settings

import com.minetoblend.osugachabot.users.UserId

interface UserSettingsService {
    fun getSettings(userId: UserId): UserSettings
    fun setReminders(userId: UserId, enabled: Boolean)
}
