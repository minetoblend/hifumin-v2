package com.minetoblend.osugachabot.cards

@JvmInline
value class CardReplicaId(val value: Long)

data class CardReplica(
    val id: CardReplicaId,
    val card: Card,
    val userId: Long,
    val condition: CardCondition,
)
