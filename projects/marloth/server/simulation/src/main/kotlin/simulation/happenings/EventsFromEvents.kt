package simulation.happenings

import simulation.combat.eventsFromAttack
import simulation.main.Deck
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
        mapEvents(::eventsFromAttack)
    )
        .flatMap { it(world, events) }
