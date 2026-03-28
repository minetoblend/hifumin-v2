package com.minetoblend.osugachabot.users.settings.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface UserSettingsRepository : JpaRepository<UserSettingsEntity, Long>
