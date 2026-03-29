package com.minetoblend.osugachabot.statuseffect

import com.minetoblend.osugachabot.users.UserId

data class StatusEffectAppliedEvent(val userId: UserId, val effect: StatusEffect)
