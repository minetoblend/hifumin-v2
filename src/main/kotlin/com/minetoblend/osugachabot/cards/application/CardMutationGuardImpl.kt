package com.minetoblend.osugachabot.cards.application

import com.minetoblend.osugachabot.cards.CardMutationGuard
import com.minetoblend.osugachabot.cards.CardReplicaId
import com.minetoblend.osugachabot.cards.MutationCheck
import com.minetoblend.osugachabot.tournament.TournamentStatus
import com.minetoblend.osugachabot.tournament.persistence.TournamentEntryRepository
import org.springframework.stereotype.Service

@Service
class CardMutationGuardImpl(
    private val tournamentEntryRepository: TournamentEntryRepository,
) : CardMutationGuard {

    override fun canMutate(cardReplicaId: CardReplicaId): MutationCheck {
        if (tournamentEntryRepository.existsByCardReplicaIdAndTournamentStatus(cardReplicaId.value, TournamentStatus.OPEN)) {
            return MutationCheck.Blocked("Card is entered in a tournament")
        }

        return MutationCheck.Allowed
    }
}
