package com.minetoblend.osugachabot.tournament.application

import com.minetoblend.osugachabot.tournament.Tournament
import com.minetoblend.osugachabot.tournament.TournamentService
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.Date
import kotlin.time.Duration.Companion.hours

@Component
class TournamentScheduler(
    private val scheduler: Scheduler,
    private val tournamentService: TournamentService,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(TournamentScheduler::class.java)

    override fun run(args: ApplicationArguments) {
        val tournament = tournamentService.ensureActiveTournament()
        scheduleTournamentResolution(tournament)
        log.info("Tournament {} scheduled for resolution", tournament.id.value)
    }

    fun scheduleTournamentResolution(tournament: Tournament) {
        val fireAt = Date(tournament.createdAt.toEpochMilli() + TOURNAMENT_DURATION.inWholeMilliseconds)

        val jobKey = JobKey.jobKey("resolve-tournament-${tournament.id.value}", GROUP)
        val triggerKey = TriggerKey.triggerKey("resolve-tournament-${tournament.id.value}", GROUP)

        val jobDetail = JobBuilder.newJob(ResolveTournamentJob::class.java)
            .withIdentity(jobKey)
            .usingJobData("tournamentId", tournament.id.value)
            .storeDurably()
            .build()

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startAt(fireAt)
            .forJob(jobKey)
            .build()

        scheduler.scheduleJob(jobDetail, setOf(trigger), true)
    }

    companion object {
        val TOURNAMENT_DURATION = 12.hours
        private const val GROUP = "tournaments"
    }
}
