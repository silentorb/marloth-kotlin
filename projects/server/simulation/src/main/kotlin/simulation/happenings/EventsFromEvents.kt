package simulation.happenings

import silentorb.mythic.characters.FreedomTable
import silentorb.mythic.characters.allCharacterMovements
import silentorb.mythic.characters.characterMovementsToImpulses
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import simulation.combat.spatial.onAttack
import simulation.combat.toSpatialCombatWorld
import simulation.combat.usingSpatialCombatWorld
import simulation.main.World
import simulation.physics.toPhysicsDeck

inline fun <reified T : GameEvent> mapEvents(crossinline transform: (T) -> Events): (Events) -> Events {
  return { events ->
    events
        .filterIsInstance<T>()
        .flatMap(transform)
  }
}

fun eventsFromEvents(world: World, freedomTable: FreedomTable, events: Events): Events {
  val deck = world.deck
  val characterMovementEvents = allCharacterMovements(toPhysicsDeck(deck), deck.characterRigs, deck.thirdPersonRigs, events)
  return listOf(
      mapEvents(onAttack(toSpatialCombatWorld(world))),
      mapEvents(eventsFromTryUseAbility(world, freedomTable))
  )
      .flatMap { it(events) }
      .plus(characterMovementEvents)
      .plus(characterMovementsToImpulses(deck.bodies, deck.characterRigs, freedomTable, characterMovementEvents))
}
