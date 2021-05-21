package simulation.happenings

import simulation.accessorize.AccessoryName
import simulation.accessorize.getAccessories
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.performing.Action
import silentorb.mythic.performing.updateCooldown
import simulation.accessorize.Accessory
import simulation.characters.EquipmentSlot
import simulation.main.Deck
import simulation.misc.Definitions
import simulation.updating.simulationDelta

fun getActions(definitions: Definitions, accessories: Table<Accessory>, actor: Id): Table<Accessory> =
    getAccessories(accessories, actor)
        .filterValues { definitions.actions.containsKey(it.type) }

fun getEquippedAction(definitions: Definitions, deck: Deck, slot: EquipmentSlot, actor: Id): Id? =
    if (slot == EquipmentSlot.utility) {
      deck.characters[actor]?.utilityItem
    } else
      getActions(definitions, deck.accessories, actor)
          .entries.firstOrNull { (_, accessory) ->
            definitions.actions[accessory.type]?.equipmentSlot == slot
          }?.key


fun updateActions(definitions: Definitions, deck: Deck, events: Events): Table<Action> {
  val actionEvents = events.filterIsInstance<UseAction>()
  val activated = actionEvents.map { it.action }
  return deck.actions.mapValues { (id, action) ->
    action.copy(
        cooldown = updateCooldown(definitions, deck, activated, id, action, simulationDelta)
    )
  }
}

fun newPossibleAction(definitions: Definitions, type: AccessoryName): Action? {
  val isActivable = definitions.actions.containsKey(type)

  return if (isActivable)
    Action(
        cooldown = 0f
    )
  else
    null
}
