package simulation.characters

import silentorb.mythic.randomly.Dice
import simulation.accessorize.AccessoryName
import simulation.accessorize.AccessoryStack
import simulation.main.NewHand
import simulation.misc.Definitions

fun equipCharacter(definitions: Definitions, dice: Dice, definition: CharacterDefinition): List<AccessoryName> {
  val pool = definition.accessoryPool
  val weaponPool = pool.intersect(definitions.weapons.keys)
  val secondaryPool = pool - weaponPool
  return listOfNotNull(
      dice.takeOneOrNull(weaponPool),
      dice.takeOneOrNull(secondaryPool),
  )
}

fun upgradeCharacterEquipment(definitions: Definitions, dice: Dice, definition: CharacterDefinition, hand: NewHand): NewHand {
  val previousAccessories = hand.children
      .mapNotNull { hand ->
        val stack = hand.components.filterIsInstance<AccessoryStack>().firstOrNull()
        if (stack != null)
          Pair(stack.value.type, Pair(stack, hand))
        else
          null
      }
      .associate { it }

  val pool = definition.accessoryPool
      .filter { definitions.accessories[it]?.maxLevel ?: 1 > previousAccessories[it]?.second?. }

}
