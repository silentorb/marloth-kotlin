package simulation.evention

import mythic.ent.Id
import simulation.combat.Damage
import simulation.combat.DamageType
import simulation.main.Hand
import simulation.misc.EntityTypeName

data class DamageEvent(
    val damage: Damage,
    val target: Id
)

interface Action {}

data class DamageAction(
    val damageType: DamageType,
    val amount: Int
) : Action

data class ApplyBuff(
    val buffType: EntityTypeName,
    val value: Int,
    val duration: Int
) : Action
