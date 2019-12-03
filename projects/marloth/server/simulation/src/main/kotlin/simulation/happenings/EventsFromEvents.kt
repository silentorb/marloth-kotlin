package simulation.happenings

import simulation.entities.eventsFromTryUseAbility
import simulation.main.World

inline fun <reified T : GameEvent> mapEvents(crossinline transform: (World) -> (T) -> Events): (World, Events) -> Events {
  return { deck, events ->
    events
        .filterIsInstance<T>()
        .flatMap(transform(deck))
  }
}

fun eventsFromEvents(world: World, events: Events): Events =
    listOf(
        mapEvents(::eventsFromTryUseAbility)
    )
        .flatMap { it(world, events) }
