package simulation.happenings

import marloth.scenery.enums.ModifierId
import silentorb.mythic.combat.DamageType
import silentorb.mythic.happenings.EventTrigger

data class DamageAction(
    val damageType: DamageType,
    val amount: Int
) : EventTrigger

data class ApplyBuff(
    val buffType: ModifierId,
    val strength: Int,
    val duration: Int
) : EventTrigger

class TakeItem: EventTrigger
