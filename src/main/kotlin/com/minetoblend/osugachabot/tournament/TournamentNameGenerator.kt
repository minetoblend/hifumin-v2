package com.minetoblend.osugachabot.tournament

object TournamentNameGenerator {

    private val prefixes = listOf(
        "osu!",
        "Global",
        "Rising",
        "Spring",
        "Summer",
        "Autumn",
        "Winter",
        "Midnight",
        "5 Digit",
        "Night Sky",
        "Circle",
        "Full Combo",
        "Speed of Light",
        "Hidden",
        "Hardrock",
        "Double Time",
        "Flashlight",
        "Aim",
        "Stream",
        "Jump",
        "Slider",
        "Taiko",
        "Catch the",
        "Mania",
        "Standard",
    )

    private val suffixes = listOf(
        "Cup",
        "Open",
        "Invitational",
        "World Cup",
        "Tournament",
        "Showdown",
        "Clash",
        "Championship",
        "Masters",
        "Throwdown",
        "Showmatch",
        "Gauntlet",
        "Arena",
    )

    fun generate(): String {
        val prefix = prefixes.random()
        val suffix = suffixes.random()
        return "$prefix $suffix"
    }
}
