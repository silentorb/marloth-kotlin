package evention

import mythic.ent.Id
import simulation.combat.Damage
import simulation.combat.DamageType

data class DamageEvent(
    val damage: Damage,
    val target: Id
)

interface Action {}

data class DamageAction(
    val type: DamageType,
    val amount: Int
) : Action
