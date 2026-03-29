package com.minetoblend.osugachabot.drops.application

import com.minetoblend.osugachabot.cooldown.CooldownResult
import com.minetoblend.osugachabot.cooldown.CooldownService
import com.minetoblend.osugachabot.cooldown.CooldownType
import com.minetoblend.osugachabot.drops.DropCreatedEvent
import com.minetoblend.osugachabot.statuseffect.StatusEffect
import com.minetoblend.osugachabot.statuseffect.StatusEffectAppliedEvent
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.Date

@Component
class DropReminderScheduler(
    private val scheduler: Scheduler,
    private val cooldownService: CooldownService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDropCreated(event: DropCreatedEvent) {
        val duration = cooldownService.durationFor(CooldownType.DROP, event.userId)
        val fireAt = Date(System.currentTimeMillis() + duration.inWholeMilliseconds)

        val jobKey = JobKey.jobKey("drop-reminder-${event.userId.value}", "drop-reminders")
        val triggerKey = TriggerKey.triggerKey("drop-reminder-${event.userId.value}", "drop-reminders")

        val jobDetail = JobBuilder.newJob(DropReminderJob::class.java)
            .withIdentity(jobKey)
            .usingJobData("userId", event.userId.value)
            .storeDurably()
            .build()

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startAt(fireAt)
            .forJob(jobKey)
            .build()

        scheduler.scheduleJob(jobDetail, setOf(trigger), true)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onStatusEffectApplied(event: StatusEffectAppliedEvent) {
        if (event.effect != StatusEffect.DropCooldownReduction) return

        val triggerKey = TriggerKey.triggerKey("drop-reminder-${event.userId.value}", "drop-reminders")
        if (!scheduler.checkExists(triggerKey)) return

        val remaining = when (val result = cooldownService.checkCooldown(event.userId, CooldownType.DROP)) {
            is CooldownResult.OnCooldown -> result.remaining
            CooldownResult.Ready -> return
        }

        val newTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startAt(Date(System.currentTimeMillis() + remaining.inWholeMilliseconds))
            .forJob(JobKey.jobKey("drop-reminder-${event.userId.value}", "drop-reminders"))
            .build()

        scheduler.rescheduleJob(triggerKey, newTrigger)
    }
}
