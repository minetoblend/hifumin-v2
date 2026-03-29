package com.minetoblend.osugachabot.statuseffect

import com.minetoblend.osugachabot.users.UserId
import kotlin.time.Duration

interface StatusEffectService {
    fun applyEffect(userId: UserId, effect: StatusEffect, duration: Duration)
    fun isActive(userId: UserId, effect: StatusEffect): Boolean
}
