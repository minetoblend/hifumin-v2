package com.minetoblend.osugachabot.cards

interface CardReplicaService {
    fun findById(id: CardReplicaId): CardReplica?
}
