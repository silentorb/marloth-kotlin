package simulation.accessorize

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Commands
import silentorb.mythic.performing.ActionDefinition
import simulation.characters.Character
import simulation.characters.EquipmentSlot
import simulation.main.Deck
import simulation.misc.Definitions

fun getUtilityItems(actions: Map<AccessoryName, ActionDefinition>, accessories: Table<AccessoryStack>, actor: Id): List<Id> =
    accessories
        .filterValues { it.owner == actor && actions[it.value.type]?.equipmentSlot == EquipmentSlot.utility }
        .entries
        .sortedBy { it.value.value.type }
        .map { it.key }

fun updateUtilityItem(definitions: Definitions, deck: Deck, commands: Commands, actor: Id, character: Character): Id? {
  val items = getUtilityItems(definitions.actions, deck.accessories, actor)
  return when {
    items.none() -> null
    items.size == 1 -> items.first()
    else -> {
      val utilityItem = if (items.contains(character.utilityItem))
        character.utilityItem
      else
        items.firstOrNull()

      val minus = commands.count { it.type == CharacterCommands.previousItem }
      val plus = commands.count { it.type == CharacterCommands.nextItem }
      val offset = plus - minus

      return if (offset == 0 || utilityItem == null)
        utilityItem
      else {
        val previousIndex = items.indexOf(utilityItem)
        val index = (previousIndex + offset + items.size) % items.size
        items[index]
      }
    }
  }
}
