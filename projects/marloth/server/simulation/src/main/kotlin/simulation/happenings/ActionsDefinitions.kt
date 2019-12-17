package simulation.happenings

import marloth.scenery.enums.ModifierId
import silentorb.mythic.ent.Id
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

class TakeItem: EventTrigger
