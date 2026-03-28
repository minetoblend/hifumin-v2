package com.minetoblend.osugachabot.cards

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "cards")
class CardEntity {
    @Id
    @GeneratedValue
    var id: Long = 0
}