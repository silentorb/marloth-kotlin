package simulation.combat

import mythic.ent.Id
import mythic.ent.Table
import simulation.entities.*
import simulation.main.Deck
import simulation.main.Percentage
import simulation.main.applyMultiplier
import simulation.misc.Definitions

enum class DamageType {
  chaos,
  cold,
  lightning,
  fire,
  physical,
  poison
}

val staticDamageTypes = DamageType.values()

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

data class ModifierDeck(
    val accessories: Table<Accessory>,
    val attachments: Table<Attachment>,
    val buffs: Table<Modifier>
)

fun toModifierDeck(deck: Deck) =
    ModifierDeck(
        accessories = deck.accessories,
        attachments = deck.attachments,
        buffs = deck.buffs
    )

enum class ModifierOperation {
  add,
  multiply
}

fun getValueModifiers(definitions: Definitions, modifierDeck: ModifierDeck, id: Id): List<Modifier> {
  val indirectModifiers = modifierDeck.attachments
      .filterValues { it.target == id && it.category == AttachmentTypeId.ability || it.category == AttachmentTypeId.equipped }
      .flatMap {
        val type = modifierDeck.accessories[id]!!.type
        definitions.accessories[type]!!.modifiers
      }
  val directModifiers = modifierDeck.attachments
      .filterValues { it.target == id && it.category == AttachmentTypeId.buff }
      .map { modifierDeck.buffs[id]!! }

  return indirectModifiers.plus(directModifiers)
}

typealias DamageModifierQuery = (Id, DamageType) -> List<Int>

fun getDamageMultiplierModifiers(definitions: Definitions, modifierDeck: ModifierDeck): DamageModifierQuery =
    { id, damageType ->
      getValueModifiers(definitions, modifierDeck, id)
          .mapNotNull {
            val definition = definitions.modifiers[it.type]!!
            val valueModifier = definition.valueModifier!!
            if (valueModifier.operation == ModifierOperation.multiply && valueModifier.subtype == damageType)
              valueModifier.value
            else
              null
          }
    }


fun calculateDamageMultipliers(modifierQuery: DamageModifierQuery, id: Id, base: DamageMultipliers): DamageMultipliers {
  return staticDamageTypes.map { damageType ->
    val baseMultipler = base[damageType] ?: 100
    val modifiers = modifierQuery(id, damageType)
    val aggregate = modifiers.sum()
    val value = baseMultipler + aggregate
    Pair(damageType, value)
  }.associate { it }
}
