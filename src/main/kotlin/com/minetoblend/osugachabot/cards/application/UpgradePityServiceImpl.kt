package com.minetoblend.osugachabot.cards.application

import com.minetoblend.osugachabot.cards.CardCondition
import com.minetoblend.osugachabot.cards.UpgradePityService
import com.minetoblend.osugachabot.cards.persistence.UpgradePityEntity
import com.minetoblend.osugachabot.cards.persistence.UpgradePityId
import com.minetoblend.osugachabot.cards.persistence.UpgradePityRepository
import com.minetoblend.osugachabot.users.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpgradePityServiceImpl(
    private val upgradePityRepository: UpgradePityRepository,
) : UpgradePityService {

    @Transactional(readOnly = true)
    override fun getPity(userId: UserId, sourceCondition: CardCondition): Int =
        upgradePityRepository.findById(UpgradePityId(userId.value, sourceCondition))
            .map { it.failures }
            .orElse(0)

    @Transactional
    override fun recordFailure(userId: UserId, sourceCondition: CardCondition): Int {
        val id = UpgradePityId(userId.value, sourceCondition)
        val entity = upgradePityRepository.findByIdForUpdate(id)
            ?: upgradePityRepository.save(UpgradePityEntity(id, failures = 0))
        entity.failures += 1
        return entity.failures
    }

    @Transactional
    override fun reset(userId: UserId, sourceCondition: CardCondition) {
        val id = UpgradePityId(userId.value, sourceCondition)
        val entity = upgradePityRepository.findByIdForUpdate(id) ?: return
        entity.failures = 0
    }
}
