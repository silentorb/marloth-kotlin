package simulation.happenings

import silentorb.mythic.characters.allCharacterMovements
import silentorb.mythic.happenings.CharacterCommand
import simulation.combat.spatial.onAttack
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
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

fun eventsFromEvents(world: World, events: Events): Events =
    listOf(
        mapEvents(::eventsFromTryUseAbility),
        mapEvents(usingSpatialCombatWorld(::onAttack))
    )
        .flatMap { it(world, events) }
        .plus(allCharacterMovements(toPhysicsDeck(world.deck), world.deck.characterRigs, events))
