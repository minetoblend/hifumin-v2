package com.minetoblend.osugachabot.cards

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CardReplicaIdTest {

    @Test
    fun `first id encodes to aaaa`() {
        assertEquals("aaaa", CardReplicaId(1).toDisplayId())
    }

    @Test
    fun `second id encodes to aaab`() {
        assertEquals("aaab", CardReplicaId(2).toDisplayId())
    }

    @Test
    fun `26th id encodes to aaaz`() {
        assertEquals("aaaz", CardReplicaId(26).toDisplayId())
    }

    @Test
    fun `27th id encodes to aaba`() {
        assertEquals("aaba", CardReplicaId(27).toDisplayId())
    }

    @Test
    fun `last 4-letter id encodes to zzzz`() {
        assertEquals("zzzz", CardReplicaId(26L * 26 * 26 * 26).toDisplayId())
    }

    @Test
    fun `first 5-letter id encodes to aaaaa`() {
        assertEquals("aaaaa", CardReplicaId(26L * 26 * 26 * 26 + 1).toDisplayId())
    }

    @Test
    fun `round-trip encode and decode`() {
        listOf(1L, 2L, 26L, 27L, 100L, 456976L, 456977L, 999999L).forEach { raw ->
            val id = CardReplicaId(raw)
            assertEquals(id, CardReplicaId.fromDisplayId(id.toDisplayId()), "round-trip failed for id=$raw")
        }
    }

    @Test
    fun `fromDisplayId parses aaaa as id 1`() {
        assertEquals(CardReplicaId(1), CardReplicaId.fromDisplayId("aaaa"))
    }

    @Test
    fun `fromDisplayId parses aaab as id 2`() {
        assertEquals(CardReplicaId(2), CardReplicaId.fromDisplayId("aaab"))
    }

    @Test
    fun `fromDisplayId returns null for empty string`() {
        assertNull(CardReplicaId.fromDisplayId(""))
    }

    @Test
    fun `fromDisplayId returns null for non-alpha characters`() {
        assertNull(CardReplicaId.fromDisplayId("aa1a"))
    }

    @Test
    fun `fromDisplayId returns null for uppercase letters`() {
        assertNull(CardReplicaId.fromDisplayId("AAAA"))
    }

    @Test
    fun `fromDisplayId returns null for string shorter than 4 chars`() {
        assertNull(CardReplicaId.fromDisplayId("aaa"))
    }
}
