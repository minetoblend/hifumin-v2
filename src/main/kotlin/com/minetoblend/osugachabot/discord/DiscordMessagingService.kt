package com.minetoblend.osugachabot.discord

import com.minetoblend.osugachabot.users.UserId

interface DiscordMessagingService {
    suspend fun sendDm(userId: UserId, message: String)

    suspend fun sendChannelMessage(channelId: Long, message: String)

    suspend fun sendChannelMessageWithImage(
        channelId: Long,
        message: String,
        imageBytes: ByteArray,
        fileName: String,
    )
}
