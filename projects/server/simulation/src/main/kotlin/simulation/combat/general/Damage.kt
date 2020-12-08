package simulation.combat.general

import simulation.accessorize.ChildAccessory
import silentorb.mythic.ent.Id

const val defaultDamageMultiplier = 100
typealias DamageType = String

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

fun aggregateDamage(multipliers: DamageMultipliers, damages: List<Damage>) =
    damages
        .map(applyDamageMods(multipliers))
        .sum()

fun aggregateHealthModifiers(destructible: Destructible, damages: List<Damage>): Int {
  val damage = aggregateDamage(destructible.damageMultipliers, damages)
  return -damage
}

enum class ModifierOperation {
  add,
  multiply
}

fun getValueModifiers(definitions: CombatDefinitions, combatDeck: CombatDeck, id: Id): List<ChildAccessory> {
  val indirectModifiers = combatDeck.accessories
      .filterValues { it.owner == id }
      .mapNotNull {
        val accessory = combatDeck.accessories[it.key]
        if (accessory != null)
          definitions.accessories[accessory.value.type]?.children
        else
          null
      }
      .flatten()

  return indirectModifiers
}

typealias DamageModifierQuery = (Id) -> (DamageType) -> List<Int>

fun getDamageMultiplierModifiers(definitions: CombatDefinitions, combatDeck: CombatDeck): DamageModifierQuery =
    { id ->
      val modifiers = getValueModifiers(definitions, combatDeck, id)
      val expressionSeparator = 0
      { damageType ->
        modifiers.mapNotNull {
          null
//          val definition = definitions.modifiers[it.type]
//          if (definition != null) {
//            val valueModifier = definition.valueModifier
//            if (valueModifier != null && valueModifier.operation == ModifierOperation.multiply
//                && valueModifier.subtype == damageType)
//              resolveValueModifier(valueModifier, it.strength)
//            else
//              null
//          } else null
        }
      }
    }

fun calculateDamageMultipliers(damageTypes: Set<DamageType>, modifierQuery: DamageModifierQuery, id: Id, base: DamageMultipliers): DamageMultipliers {
  val query = modifierQuery(id)
  return damageTypes.map { damageType ->
    val baseMultipler = base[damageType] ?: defaultDamageMultiplier
    val modifiers = query(damageType)
    val aggregate = modifiers.sum()
    val value = baseMultipler + aggregate
    Pair(damageType, value)
  }.associate { it }
}
