package simulation.combat

import mythic.ent.Id
import simulation.main.Percentage
import simulation.main.applyMultiplier

enum class DamageType {
  chaos,
  cold,
  lightning,
  fire,
  physical,
  poison
}

data class Damage(
    val type: DamageType,
    val amount: Int,
    val source: Id
)

typealias DamageMultipliers = Map<DamageType, Percentage>

fun applyDamageMods(multipliers: DamageMultipliers): (Damage) -> Int = { damage ->
  val mod = multipliers[damage.type]
  if (mod == null)
    damage.amount
  else
    applyMultiplier(damage.amount, mod)
}
