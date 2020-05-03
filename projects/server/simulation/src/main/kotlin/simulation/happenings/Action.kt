package simulation.happenings

import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.accessorize.getAccessories
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.UseAction
import silentorb.mythic.performing.Action
import silentorb.mythic.performing.updateCooldown
import silentorb.mythic.spatial.Vector3
import simulation.combat.spatial.startAttack
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions
import simulation.misc.isAtHome
import simulation.updating.simulationDelta

fun getActiveAction(deck: Deck, character: Id): Id? {
  val characterRecord = deck.characters.getValue(character)
  return characterRecord.activeAccessory
}

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

fun getActions(definitions: Definitions, accessories: Table<Accessory>, actor: Id): Table<Accessory> =
    getAccessories(accessories, actor)
        .filterValues { definitions.actions.containsKey(it.type) }
