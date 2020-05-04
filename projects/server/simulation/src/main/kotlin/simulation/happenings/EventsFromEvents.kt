package simulation.happenings

import silentorb.mythic.characters.rigs.FreedomTable
import silentorb.mythic.characters.rigs.allCharacterMovements
import silentorb.mythic.characters.rigs.characterMovementsToImpulses
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import simulation.characters.getMoveSpeedTable
import simulation.combat.spatial.onAttack
import simulation.combat.toSpatialCombatWorld
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
  val moveSpeedTable = getMoveSpeedTable(world.definitions, world.deck)
  return listOf(
      mapEvents(onAttack(toSpatialCombatWorld(world))),
      mapEvents(eventsFromTryAction(world, freedomTable))
  )
      .flatMap { it(events) }
      .plus(characterMovementEvents)
      .plus(characterMovementsToImpulses(deck.bodies, deck.characterRigs, freedomTable, moveSpeedTable, characterMovementEvents))
}
