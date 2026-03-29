package com.minetoblend.osugachabot.drops.application

import com.minetoblend.osugachabot.drops.DropReminderService
import com.minetoblend.osugachabot.users.UserId
import kotlinx.coroutines.runBlocking
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DropReminderJob(
    private val dropReminderService: DropReminderService
) : Job {
    override fun execute(context: JobExecutionContext) {
        val userId = UserId(context.mergedJobDataMap.getLong("userId"))
        runBlocking { dropReminderService.sendReminderIfEnabled(userId) }
    }
}
