package com.minetoblend.osugachabot.statuseffect.persistence

import com.minetoblend.osugachabot.statuseffect.StatusEffect
import org.springframework.data.jpa.repository.JpaRepository

interface ActiveStatusEffectRepository : JpaRepository<ActiveStatusEffectEntity, Long> {
    fun findByUserIdAndEffect(userId: Long, effect: StatusEffect): ActiveStatusEffectEntity?
}
