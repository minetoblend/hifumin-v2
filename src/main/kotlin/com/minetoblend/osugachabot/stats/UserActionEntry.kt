package com.minetoblend.osugachabot.stats

import com.minetoblend.osugachabot.users.UserId

data class UserActionEntry(
    val userId: UserId,
    val action: UserAction,
    val count: Long,
)
