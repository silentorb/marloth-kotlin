package simulation.happenings

import marloth.scenery.enums.AccessoryId
import silentorb.mythic.characters.Freedom
import silentorb.mythic.characters.FreedomTable
import silentorb.mythic.characters.hasFreedom
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.UseAction
import simulation.abilities.entangleEvents
import simulation.combat.spatial.startAttack
import simulation.combat.spatial.withResolvedTarget
import simulation.main.World

fun eventsFromTryUseAbility(world: World, event: TryUseAbilityEvent): Events {
  val deck = world.deck
  val actor = event.actor
  val action = event.action
  val target = event.target
  val accessory = deck.accessories[action]!!
  return when {

    world.definitions.weapons.containsKey(accessory.type) -> {
      listOf(startAttack(actor, action, accessory.type, event.targetLocation))
    }

    accessory.type == AccessoryId.entangle -> withResolvedTarget(world, actor, target, entangleEvents(deck))

    else -> listOf()
  } + UseAction(
      actor = actor,
      action = action,
      deferredEvents = mapOf()
  )
}

fun eventsFromTryUseAbility(world: World, freedomTable: FreedomTable): (TryUseAbilityEvent) -> Events = { event ->
  val action = event.action
  if (hasFreedom(freedomTable, event.actor, Freedom.acting) && canUse(world, action))
    eventsFromTryUseAbility(world, event)
  else
    listOf()
}
