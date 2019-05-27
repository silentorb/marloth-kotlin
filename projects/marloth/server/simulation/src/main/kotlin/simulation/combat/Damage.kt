package simulation.combat

import mythic.ent.Id

enum class DamageType {
  pierce,
  slash,
  blunt,
  heat,
  cold,
  electricity,
  poison
}

data class Damage(
    val type: DamageType,
    val amount: Int,
    val source: Id
)

typealias DamageMap = Map<DamageType, Int> // In percentages

typealias DamageModifier = (Damage) -> Damage

fun applyDamageMap(map: DamageMap): DamageModifier = { damage ->
  val mod = map[damage.type]
  if (mod == null)
    damage
  else
    damage.copy(
        amount = damage.amount * mod / 100
    )
}
