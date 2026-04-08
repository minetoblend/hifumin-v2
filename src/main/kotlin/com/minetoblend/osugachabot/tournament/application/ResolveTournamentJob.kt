package com.minetoblend.osugachabot.tournament.application

import com.minetoblend.osugachabot.tournament.TournamentId
import com.minetoblend.osugachabot.tournament.TournamentService
import kotlinx.coroutines.runBlocking
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class ResolveTournamentJob(
    private val tournamentService: TournamentService,
    private val tournamentNotificationService: TournamentNotificationService,
    private val tournamentScheduler: TournamentScheduler,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val tournamentId = TournamentId(context.mergedJobDataMap.getLong("tournamentId"))

        val resolution = tournamentService.resolveTournament(tournamentId) ?: return

        runBlocking {
            tournamentNotificationService.notifyParticipants(resolution)
        }

        val nextTournament = tournamentService.ensureActiveTournament()
        tournamentScheduler.scheduleTournamentResolution(nextTournament)
    }
}
