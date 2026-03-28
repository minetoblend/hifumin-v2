package com.minetoblend.osugachabot.drops.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "drops")
class DropEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @OneToMany(mappedBy = "drop", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @OrderBy("cardIndex ASC")
    var cards: MutableList<DroppedCardEntity> = mutableListOf()

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}
