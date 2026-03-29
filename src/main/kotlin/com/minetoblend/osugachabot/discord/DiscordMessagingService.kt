package com.minetoblend.osugachabot.discord

import com.minetoblend.osugachabot.users.UserId

interface DiscordMessagingService {
    suspend fun sendDm(userId: UserId, message: String)
}
