package simulation.happenings

import silentorb.mythic.characters.rigs.Freedom
import silentorb.mythic.characters.rigs.FreedomTable
import silentorb.mythic.characters.rigs.hasFreedom
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.UseAction
import simulation.abilities.*
import simulation.combat.spatial.startAttack
import simulation.combat.spatial.withResolvedTarget
import simulation.main.World

fun eventsFromTryAction(world: World, event: TryActionEvent): Events {
  val definitions = world.definitions
  val deck = world.deck
  val actor = event.actor
  val action = event.action
  val target = event.target
  val accessory = deck.accessories[action]!!
  val type = accessory.value.type
  val isWeapon = definitions.weapons.containsKey(type)
  val actionDefinition = definitions.actions[type]
  val specificEvents =
      when {
        isWeapon -> listOf(startAttack(definitions.actions[type]!!, actor, action, type, event.targetLocation))
        actionDefinition != null -> when (actionDefinition.type) {
          Actions.dash -> dashEvents(definitions, accessory.value, actor)
          Actions.entangle -> withResolvedTarget(world, actor, target, entangleEvents(deck, accessory.value))
          Actions.shadowSpirit -> onShadowSpirit(world, actionDefinition, actor)
          Actions.cancelShadowSpirit -> onCancelShadowSpirit(deck, actor)
          else -> listOf()
        }
        else -> listOf()
      }

  return specificEvents + UseAction(
      actor = actor,
      action = action,
      deferredEvents = mapOf()
  )
}

fun eventsFromTryAction(world: World, freedomTable: FreedomTable): (TryActionEvent) -> Events = { event ->
  val action = event.action
  if (hasFreedom(freedomTable, event.actor, Freedom.acting) && canUse(world, action))
    eventsFromTryAction(world, event)
  else
    listOf()
}
