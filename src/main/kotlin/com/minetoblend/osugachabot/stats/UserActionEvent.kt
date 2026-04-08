package com.minetoblend.osugachabot.stats

import com.minetoblend.osugachabot.users.UserId

data class UserActionEvent(val action: UserAction, val userId: UserId)
