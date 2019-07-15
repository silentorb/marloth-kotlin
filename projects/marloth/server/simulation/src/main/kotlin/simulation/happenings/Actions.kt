package simulation.happenings

import scenery.enums.ModifierId
import simulation.combat.DamageType

interface Action {}

data class DamageAction(
    val damageType: DamageType,
    val amount: Int
) : Action

data class ApplyBuff(
    val buffType: ModifierId,
    val strength: Int,
    val duration: Int
) : Action
