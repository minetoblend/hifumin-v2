package com.minetoblend.osugachabot.cards

import kotlin.test.Test
import kotlin.test.assertEquals

class CardRarityTest {

    @Test
    fun `fromFollowerCount returns Common for low follower counts`() {
        assertEquals(CardRarity.Common, CardRarity.fromFollowerCount(0))
        assertEquals(CardRarity.Common, CardRarity.fromFollowerCount(500))
        assertEquals(CardRarity.Common, CardRarity.fromFollowerCount(999))
    }

    @Test
    fun `fromFollowerCount returns Uncommon for 1k to 9999 followers`() {
        assertEquals(CardRarity.Uncommon, CardRarity.fromFollowerCount(1_000))
        assertEquals(CardRarity.Uncommon, CardRarity.fromFollowerCount(5_000))
        assertEquals(CardRarity.Uncommon, CardRarity.fromFollowerCount(9_999))
    }

    @Test
    fun `fromFollowerCount returns Rare for 10k to 99999 followers`() {
        assertEquals(CardRarity.Rare, CardRarity.fromFollowerCount(10_000))
        assertEquals(CardRarity.Rare, CardRarity.fromFollowerCount(50_000))
        assertEquals(CardRarity.Rare, CardRarity.fromFollowerCount(99_999))
    }

    @Test
    fun `fromFollowerCount returns Legendary for 100k to 499999 followers`() {
        assertEquals(CardRarity.Legendary, CardRarity.fromFollowerCount(100_000))
        assertEquals(CardRarity.Legendary, CardRarity.fromFollowerCount(250_000))
        assertEquals(CardRarity.Legendary, CardRarity.fromFollowerCount(499_999))
    }

    @Test
    fun `fromFollowerCount returns Mythic for 500k or more followers`() {
        assertEquals(CardRarity.Mythic, CardRarity.fromFollowerCount(500_000))
        assertEquals(CardRarity.Mythic, CardRarity.fromFollowerCount(1_000_000))
    }
}
