package com.minetoblend.osugachabot.tournament.persistence

import com.minetoblend.osugachabot.tournament.TournamentBracket
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import tools.jackson.databind.ObjectMapper

@Converter
class TournamentBracketConverter(
    private val objectMapper: ObjectMapper,
) : AttributeConverter<TournamentBracket?, String?> {

    override fun convertToDatabaseColumn(attribute: TournamentBracket?): String? {
        return attribute?.let { objectMapper.writeValueAsString(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): TournamentBracket? {
        return dbData?.let { objectMapper.readValue(it, TournamentBracket::class.java) }
    }
}
