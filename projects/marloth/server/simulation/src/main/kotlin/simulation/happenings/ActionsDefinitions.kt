package simulation.happenings

import scenery.enums.ModifierId
import simulation.combat.DamageType

interface EventTrigger {}

data class DamageAction(
    val damageType: DamageType,
    val amount: Int
) : EventTrigger

data class ApplyBuff(
    val buffType: ModifierId,
    val strength: Int,
    val duration: Int
) : EventTrigger

data class TakeItem(
    val placeholder: Int = 0
): EventTrigger
