package simulation.misc

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import marloth.scenery.enums.AccessoryId
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.combat.spatial.startRaycastAttack
import silentorb.mythic.happenings.Events
import simulation.happenings.TryUseAbilityEvent
import silentorb.mythic.happenings.UseAction
import silentorb.mythic.performing.Action
import silentorb.mythic.performing.updateCooldown
import simulation.main.Deck
import simulation.main.World
import simulation.updating.simulationDelta

fun canUse(deck: Deck, action: Id): Boolean {
  val actionRecord = deck.actions[action]
  return actionRecord != null && actionRecord.cooldown == 0f
}

fun getActiveAction(deck: Deck, character: Id): Id? {
  val characterRecord = deck.characters.getValue(character)
  return characterRecord.activeAccessory
}

fun updateActions(definitions: Definitions, deck: Deck, events: Events): Table<Action> {
  val actionEvents = events.filterIsInstance<UseAction>()
  val activated = actionEvents.map { it.action }
  return deck.actions.mapValues { (id, action) ->
    val isActivated = activated.contains(id)
    val accessory = deck.accessories[id]!!
    val definition = definitions.actions[accessory.type]!!
    val cooldown = definition.cooldown
    action.copy(
        cooldown = updateCooldown(action, isActivated, cooldown, simulationDelta)
    )
  }
}

fun eventsFromTryUseAbility(world: World): (TryUseAbilityEvent) -> Events = { event ->
  val deck = world.deck
  val action = getActiveAction(deck, event.actor)
  if (action != null && canUse(deck, action)) {
    val accessory = deck.accessories[action]!!
    when (accessory.type) {
      AccessoryId.pistol.name -> startRaycastAttack(event.actor, action, accessory.type)
      else -> listOf()
    }
  } else
    listOf()
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
