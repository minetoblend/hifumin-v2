package com.minetoblend.osugachabot.users

import dev.kord.common.entity.Snowflake

@JvmInline
value class UserId(val value: Long)

fun Snowflake.toUserId() = UserId(value.toLong())