package com.minetoblend.osugachabot.users.settings.application

import com.minetoblend.osugachabot.users.UserId
import com.minetoblend.osugachabot.users.settings.UserSettings
import com.minetoblend.osugachabot.users.settings.UserSettingsService
import com.minetoblend.osugachabot.users.settings.persistence.UserSettingsEntity
import com.minetoblend.osugachabot.users.settings.persistence.UserSettingsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserSettingsServiceImpl(
    private val repository: UserSettingsRepository,
) : UserSettingsService {

    override fun getSettings(userId: UserId): UserSettings =
        repository.findByIdOrNull(userId.value)?.toDomain()
            ?: UserSettings.Default

    @Transactional
    override fun setReminders(userId: UserId, enabled: Boolean) {
        val entity = repository.findByIdOrNull(userId.value) ?: UserSettings.Default.toEntity(userId)
        entity.reminders = enabled
        repository.save(entity)
    }

    private fun UserSettingsEntity.toDomain() =
        UserSettings(
            reminders = reminders,
        )

    private fun UserSettings.toEntity(userId: UserId): UserSettingsEntity = UserSettingsEntity(userId.value, reminders)
}
