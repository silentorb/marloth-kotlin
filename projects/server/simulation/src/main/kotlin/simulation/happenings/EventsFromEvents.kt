package simulation.happenings

import silentorb.mythic.characters.FreedomTable
import silentorb.mythic.characters.allCharacterMovements
import silentorb.mythic.characters.characterMovementsToImpulses
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import simulation.combat.spatial.onAttack
import simulation.combat.usingSpatialCombatWorld
import simulation.main.World
import simulation.physics.toPhysicsDeck

inline fun <reified T : GameEvent> mapEvents(crossinline transform: (World) -> (T) -> Events): (World, Events) -> Events {
  return { deck, events ->
    events
        .filterIsInstance<T>()
        .flatMap(transform(deck))
  }
}

fun eventsFromEvents(world: World, freedomTable: FreedomTable, events: Events): Events {
  val deck = world.deck
  val characterMovementEvents = allCharacterMovements(toPhysicsDeck(deck), deck.characterRigs, deck.thirdPersonRigs, events)
  return listOf(
      mapEvents(::eventsFromTryUseAbility),
      mapEvents(usingSpatialCombatWorld(::onAttack))
  )
      .flatMap { it(world, events) }
      .plus(characterMovementEvents)
      .plus(characterMovementsToImpulses(deck.bodies, deck.characterRigs, freedomTable, characterMovementEvents))
}
