package com.minetoblend.osugachabot.cards

import com.minetoblend.osugachabot.users.UserId

/**
 * Tracks consecutive failed upgrade attempts per user and source condition so that
 * players receive a guaranteed successful upgrade after enough failures.
 */
interface UpgradePityService {
    /** Returns the current number of consecutive failed upgrades for the given source condition. */
    fun getPity(userId: UserId, sourceCondition: CardCondition): Int

    /** Increments the pity counter for the given user and source condition, returning the new value. */
    fun recordFailure(userId: UserId, sourceCondition: CardCondition): Int

    /** Resets the pity counter for the given user and source condition to zero. */
    fun reset(userId: UserId, sourceCondition: CardCondition)
}
