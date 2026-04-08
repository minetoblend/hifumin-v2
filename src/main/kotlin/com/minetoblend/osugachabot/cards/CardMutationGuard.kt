package com.minetoblend.osugachabot.cards

interface CardMutationGuard {
    fun canMutate(cardReplicaId: CardReplicaId): MutationCheck
}

sealed interface MutationCheck {
    data object Allowed : MutationCheck
    data class Blocked(val reason: String) : MutationCheck
}
