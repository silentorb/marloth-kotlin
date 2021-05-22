package simulation.characters

import silentorb.mythic.randomly.Dice
import simulation.accessorize.AccessoryName
import simulation.accessorize.Accessory
import simulation.main.NewHand
import simulation.misc.Definitions

fun equipCharacter(definitions: Definitions, dice: Dice, definition: CharacterDefinition): List<AccessoryName> {
  val pool = definition.accessoryPool
  val weaponPool = pool.intersect(definitions.weapons.keys)
  val secondaryPool = pool - weaponPool
  return listOfNotNull(
      dice.takeOneOrNull(weaponPool),
      if (secondaryPool.any() && dice.getInt(10) > 4) dice.takeOne(secondaryPool) else null,
  )
}

fun upgradeCharacterEquipment(definitions: Definitions, dice: Dice, characterDefinition: CharacterDefinition, hand: NewHand): NewHand {
  assert(hand.id != null)
  val previousAccessories = hand.children
      .mapNotNull { accessoryHand ->
        val stack = accessoryHand.components.filterIsInstance<Accessory>().firstOrNull()
        if (stack != null)
          Pair(stack.type, Pair(stack, accessoryHand))
        else
          null
      }
      .associate { it }

  val maxAccessoryTypes = 3
  val basePool = if (previousAccessories.size < maxAccessoryTypes)
    characterDefinition.accessoryPool
  else
    previousAccessories.keys

  val pool = basePool
      .flatMap {
        val definition = definitions.accessories[it]!!
        definition.upgrades
      }

  val accessory = dice.takeOneOrNull(pool)
  return if (accessory != null) {
    val previous = previousAccessories[accessory]
    val withoutPrevious = if (previous != null && previous.second.id != null)
      hand.children.filter { it.id != previous.second.id }
    else
      hand.children

    hand.copy(
        children = withoutPrevious + listOfNotNull(
            newAccessory(definitions, accessory, hand.id!!)
        )
    )
  } else
    hand
}
