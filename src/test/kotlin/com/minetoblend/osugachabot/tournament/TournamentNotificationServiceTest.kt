package com.minetoblend.osugachabot.tournament

import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.discord.DiscordMessagingService
import com.minetoblend.osugachabot.graphics.CardRenderer
import com.minetoblend.osugachabot.tournament.application.TournamentNotificationService
import com.minetoblend.osugachabot.users.UserId
import io.opentelemetry.api.OpenTelemetry
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TournamentNotificationServiceTest {

    private data class ChannelMessage(val channelId: Long, val message: String, val hasImage: Boolean)

    private val channelMessages = mutableListOf<ChannelMessage>()
    private val dms = mutableListOf<Pair<UserId, String>>()

    private val messagingService = object : DiscordMessagingService {
        override suspend fun sendDm(userId: UserId, message: String) {
            dms.add(userId to message)
        }

        override suspend fun sendChannelMessage(channelId: Long, message: String) {
            channelMessages.add(ChannelMessage(channelId, message, hasImage = false))
        }

        override suspend fun sendChannelMessageWithImage(
            channelId: Long,
            message: String,
            imageBytes: ByteArray,
            fileName: String,
        ) {
            channelMessages.add(ChannelMessage(channelId, message, hasImage = true))
        }
    }

    private val service = TournamentNotificationService(messagingService, CardRenderer(OpenTelemetry.noop()))

    private fun entry(userId: Long, channelId: Long, guildId: Long? = 1L) = TournamentEntry(
        id = userId,
        tournamentId = TournamentId(1L),
        userId = UserId(userId),
        cardReplicaId = CardReplicaId(userId),
        channelId = channelId,
        guildId = guildId,
        weight = 100.0,
    )

    private fun placement(userId: Long, place: Int = 1) = TournamentPlacement(
        id = userId,
        tournamentId = TournamentId(1L),
        place = place,
        userId = UserId(userId),
        prizeGold = 1000L,
        prizeCardReplicaId = null,
    )

    private fun resolution(entries: List<TournamentEntry>, placements: List<TournamentPlacement>) =
        TournamentResolution(
            tournament = Tournament(
                id = TournamentId(1L),
                name = "Test Cup",
                status = TournamentStatus.RESOLVED,
                createdAt = Instant.now(),
                resolvedAt = Instant.now(),
                bracket = null,
                entries = entries,
            ),
            placements = placements,
        )

    private fun setup() {
        channelMessages.clear()
        dms.clear()
    }

    @Test
    fun `multiple entries from same guild produce exactly one channel message`() = runBlocking {
        setup()
        val entries = listOf(entry(1L, channelId = 100L, guildId = 1L), entry(2L, channelId = 100L, guildId = 1L))
        val res = resolution(entries, placements = listOf(placement(1L, 1), placement(2L, 2)))

        service.notifyParticipants(res)

        assertEquals(1, channelMessages.size, "Expected exactly one message for the guild")
    }

    @Test
    fun `entries from same guild but different channels produce one message`() = runBlocking {
        setup()
        val entries = listOf(entry(1L, channelId = 100L, guildId = 1L), entry(2L, channelId = 200L, guildId = 1L))
        val res = resolution(entries, placements = listOf(placement(1L, 1), placement(2L, 2)))

        service.notifyParticipants(res)

        assertEquals(1, channelMessages.size, "Same guild, different channels should still produce one message")
    }

    @Test
    fun `entries from different guilds produce one message per guild`() = runBlocking {
        setup()
        val entries = listOf(
            entry(1L, channelId = 100L, guildId = 1L),
            entry(2L, channelId = 200L, guildId = 2L),
        )
        val res = resolution(entries, placements = listOf(placement(1L, 1), placement(2L, 2)))

        service.notifyParticipants(res)

        assertEquals(2, channelMessages.size, "Each guild should receive exactly one message")
    }

    @Test
    fun `guild message mentions all participants in that guild`() = runBlocking {
        setup()
        val entries = listOf(entry(1L, channelId = 100L, guildId = 1L), entry(2L, channelId = 100L, guildId = 1L))
        val res = resolution(entries, placements = listOf(placement(1L, 1), placement(2L, 2)))

        service.notifyParticipants(res)

        val message = channelMessages.single().message
        assertTrue(message.contains("<@1>"), "Message should mention user 1")
        assertTrue(message.contains("<@2>"), "Message should mention user 2")
    }

    @Test
    fun `dm entries are sent individually without channel messages`() = runBlocking {
        setup()
        val entries = listOf(
            entry(1L, channelId = 0L, guildId = null),
            entry(2L, channelId = 0L, guildId = null),
        )
        val res = resolution(entries, placements = listOf(placement(1L, 1), placement(2L, 2)))

        service.notifyParticipants(res)

        assertEquals(0, channelMessages.size, "No channel messages for DM-only entries")
        assertEquals(2, dms.size, "Each DM entry gets an individual DM")
    }

    @Test
    fun `mixed guild and dm entries produce one guild message and individual dms`() = runBlocking {
        setup()
        val entries = listOf(
            entry(1L, channelId = 100L, guildId = 1L),
            entry(2L, channelId = 100L, guildId = 1L),
            entry(3L, channelId = 0L, guildId = null),
        )
        val res = resolution(entries, placements = listOf(placement(1L, 1)))

        service.notifyParticipants(res)

        assertEquals(1, channelMessages.size, "Guild entries produce one combined channel message")
        assertEquals(1, dms.size, "DM entry gets one DM")
    }
}
