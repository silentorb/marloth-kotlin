package simulation.happenings

import silentorb.mythic.accessorize.AccessoryName
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

fun canUse(world: World, action: Id): Boolean {
  val deck = world.deck
  val definitions = world.definitions

  val accessory = deck.accessories[action]!!
  val isWeapon = definitions.weapons.containsKey(accessory.type)
  if (isWeapon && isAtHome(world.realm.grid, deck)(accessory.owner))
    return false

  val actionRecord = deck.actions[action]
  return actionRecord != null && actionRecord.cooldown == 0f
}

fun canUseSimple(deck: Deck, action: Id): Boolean {
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
    action.copy(
        cooldown = updateCooldown(definitions, deck, activated, id, action, simulationDelta)
    )
  }
}

fun getActionTarget(deck: Deck, character: Id): Vector3? {
  val spirit = deck.spirits[character]
  // Cheat a little since the AI target position memory may be a little outdated
  // and in this case a little cheating could just be seen as anticipating
  return deck.bodies[spirit?.pursuit?.targetEnemy]?.position ?: spirit?.pursuit?.targetPosition
}

fun eventsFromTryUseAbility(world: World): (TryUseAbilityEvent) -> Events = { event ->
  val deck = world.deck
  val actor = event.actor
  val action = getActiveAction(deck, actor)
  val isPlayer = world.deck.players.containsKey(actor)
  if (action != null && canUse(world, action)) {
    val accessory = deck.accessories[action]!!
    when {

      world.definitions.weapons.containsKey(accessory.type) -> {
        val target = getActionTarget(deck, actor)
        listOf(startAttack(actor, action, accessory.type, target))
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
