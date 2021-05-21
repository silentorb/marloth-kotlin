package simulation.happenings

import silentorb.mythic.characters.rigs.Freedom
import silentorb.mythic.characters.rigs.FreedomTable
import silentorb.mythic.characters.rigs.hasFreedom
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import simulation.abilities.*
import simulation.combat.general.ModifyResource
import simulation.combat.spatial.startAttack
import simulation.combat.spatial.withResolvedTarget
import simulation.main.World

data class UseAction(
    val actor: Id,
    val action: Id,
    val deferredEvents: Map<String, GameEvent> = mapOf()
)

fun eventsFromTryAction(world: World, event: TryActionEvent): Events {
  val definitions = world.definitions
  val deck = world.deck
  val actor = event.actor
  val action = event.action
  val targetEntity = event.targetEntity
  val accessory = deck.accessories[action]!!
  val type = accessory.type
  val isWeapon = definitions.weapons.containsKey(type)
  val actionDefinition = definitions.actions[type]
  val specificEvents =
      when {
        isWeapon -> listOf(startAttack(definitions.actions[type]!!, actor, action, type, event.targetLocation, event.targetEntity))
        actionDefinition != null -> when (actionDefinition.type) {
          Actions.dash -> dashEvents(definitions, accessory, actor)
          Actions.entangle -> withResolvedTarget(world, actor, targetEntity, entangleEvents(deck, accessory))
          Actions.shadowSpirit -> onShadowSpirit(world, actionDefinition, actor)
          Actions.cancelShadowSpirit -> onCancelShadowSpirit(deck, actor)
          else -> listOf()
        }
        else -> listOf()
      }

  val cost = actionDefinition?.cost
  val paymentEvents = if (cost != null)
    listOf(
        ModifyResource(
            actor = actor,
            resource = cost.type,
            amount = -cost.amount,
        )
    )
  else
    listOf()

  return specificEvents + UseAction(
      actor = actor,
      action = action,
      deferredEvents = mapOf()
  ) + paymentEvents
}

fun eventsFromTryAction(world: World, freedomTable: FreedomTable): (TryActionEvent) -> Events = { event ->
  val action = event.action
  if (hasFreedom(freedomTable, event.actor, Freedom.acting) && canUse(world, action))
    eventsFromTryAction(world, event)
  else
    listOf()
}
