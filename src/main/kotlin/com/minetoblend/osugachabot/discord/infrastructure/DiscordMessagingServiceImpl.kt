package com.minetoblend.osugachabot.discord.infrastructure

import com.minetoblend.osugachabot.discord.DiscordMessagingService
import com.minetoblend.osugachabot.users.UserId
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.MessageChannel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DiscordMessagingServiceImpl(
    @Autowired(required = false) private val kord: Kord?,
) : DiscordMessagingService {

    override suspend fun sendDm(userId: UserId, message: String) {
        val kord = kord ?: return
        val user = kord.getUser(Snowflake(userId.value.toULong())) ?: return
        val channel = user.getDmChannelOrNull() ?: return
        channel.createMessage(message)
    }

    override suspend fun sendChannelMessage(channelId: Long, message: String) {
        val kord = kord ?: return
        val channel = kord.getChannel(Snowflake(channelId.toULong())) as? MessageChannel ?: return
        channel.createMessage(message)
    }
}
