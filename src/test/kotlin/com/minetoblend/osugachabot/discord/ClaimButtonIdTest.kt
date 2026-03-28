package com.minetoblend.osugachabot.discord

import com.minetoblend.osugachabot.discord.application.drop.ClaimButtonId
import com.minetoblend.osugachabot.drops.DropId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClaimButtonIdTest {

    @Test
    fun `toCustomId produces expected format`() {
        val id = ClaimButtonId(DropId(42), cardIndex = 1)

        assertEquals("drop:claim:42:1", id.toCustomId())
    }

    @Test
    fun `fromString parses a valid custom id`() {
        val result = ClaimButtonId.fromString("drop:claim:42:1")

        assertNotNull(result)
        assertEquals(DropId(42), result.dropId)
        assertEquals(1, result.cardIndex)
    }

    @Test
    fun `fromString round-trips with toCustomId`() {
        val original = ClaimButtonId(DropId(99), cardIndex = 2)

        val result = ClaimButtonId.fromString(original.toCustomId())

        assertEquals(original, result)
    }

    @Test
    fun `fromString returns null for wrong prefix`() {
        assertNull(ClaimButtonId.fromString("other:command:1:0"))
    }

    @Test
    fun `fromString returns null for non-numeric drop id`() {
        assertNull(ClaimButtonId.fromString("drop:claim:abc:0"))
    }

    @Test
    fun `fromString returns null for non-numeric card index`() {
        assertNull(ClaimButtonId.fromString("drop:claim:1:xyz"))
    }

    @Test
    fun `fromString returns null when card index part is missing`() {
        assertNull(ClaimButtonId.fromString("drop:claim:42"))
    }

    @Test
    fun `fromString returns null for empty string`() {
        assertNull(ClaimButtonId.fromString(""))
    }

    @Test
    fun `isValid returns true for correct prefix`() {
        assertTrue(ClaimButtonId.isValid("drop:claim:42:1"))
    }

    @Test
    fun `isValid returns false for wrong prefix`() {
        assertFalse(ClaimButtonId.isValid("other:command:1:0"))
    }

    @Test
    fun `isValid returns true for malformed suffix with correct prefix`() {
        // isValid is a fast prefix check — fromString is the authoritative parse
        assertTrue(ClaimButtonId.isValid("drop:claim:not-a-number"))
        assertNull(ClaimButtonId.fromString("drop:claim:not-a-number"))
    }
}
