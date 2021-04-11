package simulation.happenings

import marloth.scenery.enums.AccessoryIdOld
import silentorb.mythic.characters.rigs.Freedom
import silentorb.mythic.characters.rigs.FreedomTable
import silentorb.mythic.characters.rigs.hasFreedom
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.UseAction
import simulation.abilities.Actions
import simulation.abilities.dashEvents
import simulation.abilities.entangleEvents
import simulation.abilities.onShadowSpirit
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
      if (isWeapon)
        listOf(startAttack(definitions.actions[type]!!, actor, action, type, event.targetLocation))
      else when (accessory.value.type) {
        AccessoryIdOld.dash -> dashEvents(definitions, accessory.value, actor)
        AccessoryIdOld.entangle -> withResolvedTarget(world, actor, target, entangleEvents(deck, accessory.value))
        else -> if (actionDefinition != null)
          when(actionDefinition.type) {
          Actions.shadowSpirit -> onShadowSpirit(world, actionDefinition, actor)
          else -> listOf()
        }
        else
          listOf()
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
