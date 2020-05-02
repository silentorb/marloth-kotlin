package simulation.happenings

import marloth.scenery.enums.ModifierId
import silentorb.mythic.accessorize.AccessoryName
import simulation.combat.general.DamageType
import silentorb.mythic.happenings.EventTrigger

data class DamageAction(
    val damageType: DamageType,
    val amount: Int
) : EventTrigger

data class ApplyBuff(
    val buffType: AccessoryName,
    val strength: Int,
    val duration: Int
) : EventTrigger

class TakeItem: EventTrigger
