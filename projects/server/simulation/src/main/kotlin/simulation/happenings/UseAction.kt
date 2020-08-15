package simulation.happenings

import marloth.scenery.enums.AccessoryId
import silentorb.mythic.characters.rigs.Freedom
import silentorb.mythic.characters.rigs.FreedomTable
import silentorb.mythic.characters.rigs.hasFreedom
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.UseAction
import simulation.abilities.dashEvents
import simulation.abilities.entangleEvents
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
  val isWeapon = definitions.weapons.containsKey(accessory.type)
  val specificEvents =
      if (isWeapon)
        listOf(startAttack(definitions.actions[accessory.type]!!, actor, action, accessory.type, event.targetLocation))
      else when (accessory.type) {
        AccessoryId.dash -> dashEvents(definitions, accessory, actor)
        AccessoryId.entangle -> withResolvedTarget(world, actor, target, entangleEvents(deck, accessory))
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
