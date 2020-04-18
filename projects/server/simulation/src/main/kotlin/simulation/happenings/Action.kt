package simulation.happenings

import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.combat.spatial.AttackEvent
import silentorb.mythic.combat.spatial.onAttack
import silentorb.mythic.combat.spatial.startAttack
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.UseAction
import silentorb.mythic.performing.Action
import silentorb.mythic.performing.updateCooldown
import silentorb.mythic.spatial.Vector3
import simulation.combat.toSpatialCombatWorld
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions
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

fun getActionTarget(deck: Deck, character: Id): Vector3? {
  val spirit = deck.spirits[character]
  return spirit?.pursuit?.targetPosition
}

fun eventsFromTryUseAbility(world: World): (TryUseAbilityEvent) -> Events = { event ->
  val deck = world.deck
  val action = getActiveAction(deck, event.actor)
  val isPlayer = world.deck.players.containsKey(event.actor)
  if (action != null && canUse(deck, action)) {
    val accessory = deck.accessories[action]!!
    when {

      world.definitions.weapons.containsKey(accessory.type) -> {
        val target = getActionTarget(deck, event.actor)
        if (isPlayer)
          onAttack(toSpatialCombatWorld(world))(AttackEvent(event.actor, accessory.type, target))
        else
          listOf(startAttack(event.actor, action, accessory.type, target))
      }

      else -> listOf()
    } + UseAction(
        actor = event.actor,
        action = action,
        deferredEvents = mapOf()
    )
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
