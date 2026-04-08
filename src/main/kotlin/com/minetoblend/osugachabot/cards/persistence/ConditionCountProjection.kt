package com.minetoblend.osugachabot.cards.persistence

interface ConditionCountProjection {
    fun getUserId(): Long
    fun getCount(): Long
}
