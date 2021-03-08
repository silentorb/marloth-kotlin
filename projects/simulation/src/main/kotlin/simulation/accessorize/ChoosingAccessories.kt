package simulation.accessorize

import silentorb.mythic.ent.Id
import silentorb.mythic.randomly.Dice
import simulation.characters.AccessoryOptions
import simulation.main.Deck
import simulation.misc.Definitions

data class ChooseImprovedAccessory(
    val actor: Id,
    val accessory: AccessoryName
)

fun newAccessoryChoice(definitions: Definitions, dice: Dice, deck: Deck, actor: Id): AccessoryOptions {
  val currentAccessories = getAccessories(deck.accessories, actor)
  val available = definitions.selectableAccessories
      .filter { accessory ->
        val definition = definitions.accessories[accessory]!!
        val currentLevel = currentAccessories.values.firstOrNull { it.value.type == accessory }?.value?.level ?: 0
        definition.maxLevel > currentLevel
      }

  return dice.take(available, 2)
}

//fun newChosenAccessories(accessories: Table<AccessoryStack>, events: Events): List<Hand> {
//  val chooseAccessoryEvents = events.filterIsInstance<ChooseImprovedAccessory>()
//  return chooseAccessoryEvents
//      .filter { event -> !hasAccessory(event.accessory, accessories, event.actor) }
//      .map { event ->
//        Hand(
//            accessory = AccessoryStack(
//                owner = event.actor,
//                type = event.accessory
//            )
//        )
//      }
//}
