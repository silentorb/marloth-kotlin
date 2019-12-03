package simulation.entities

import mythic.ent.Id
import mythic.ent.Table
import scenery.enums.AccessoryId
import simulation.combat.raycastAttack
import simulation.happenings.Events
import simulation.happenings.TryUseAbilityEvent
import simulation.happenings.UseAction
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions
import simulation.updating.simulationDelta

data class ActionDefinition(
    val cooldown: Float,
    val range: Float
)

data class Action(
    val cooldown: Float = 0f
)

data class NewAbility(
    val id: Id
)

fun updateCooldown(action: Action, isActivated: Boolean, cooldown: Float, delta: Float): Float {
  return if (isActivated)
    cooldown
  else if (action.cooldown > 0f)
    Math.max(0f, action.cooldown - delta)
  else
    0f
}

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
  return deck.actions.mapValues { (id, ability) ->
    val isActivated = activated.contains(id)
    val accessory = deck.accessories[id]!!
    val definition = definitions.accessories[accessory.type]!!
    val cooldown = definition.activation!!.cooldown
    ability.copy(
        cooldown = updateCooldown(ability, isActivated, cooldown, simulationDelta)
    )
  }
}

fun eventsFromTryUseAbility(world: World): (TryUseAbilityEvent) -> Events = { event ->
  val deck = world.deck
  val action = getActiveAction(deck, event.actor)
  if (action != null && canUse(deck, action)) {
    val accessory = deck.accessories[action]!!
    when (accessory.type) {
      AccessoryId.pistol.name -> raycastAttack(world, event.actor, action)
      else -> listOf()
    }
  } else
    listOf()
}
