package com.minetoblend.osugachabot.cards.persistence

import com.minetoblend.osugachabot.cards.CardCondition
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.io.Serializable

@Embeddable
data class UpgradePityId(
    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,
    @Column(name = "source_condition", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var sourceCondition: CardCondition = CardCondition.Damaged,
) : Serializable

@Entity
@Table(name = "upgrade_pity")
class UpgradePityEntity(
    @EmbeddedId
    var id: UpgradePityId,
    @Column(nullable = false)
    var failures: Int = 0,
)
