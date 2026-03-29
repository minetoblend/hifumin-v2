package com.minetoblend.osugachabot.drops

import com.minetoblend.osugachabot.users.UserId

interface DropReminderService {
    suspend fun sendReminderIfEnabled(userId: UserId)
}
