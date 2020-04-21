package simulation.happenings

import simulation.combat.spatial.onAttack
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import simulation.combat.usingSpatialCombatWorld
import simulation.entities.eventsFromRespawnCountdowns
import simulation.main.Deck
import simulation.main.World

inline fun <reified T : GameEvent> mapEvents(crossinline transform: (World) -> (T) -> Events): (World, Events) -> Events {
  return { deck, events ->
    events
        .filterIsInstance<T>()
        .flatMap(transform(deck))
  }
}

fun eventsFromEvents(previous: Deck, world: World, events: Events): Events =
    listOf(
        mapEvents(::eventsFromTryUseAbility),
        mapEvents(usingSpatialCombatWorld(::onAttack))
    )
        .flatMap { it(world, events) }
        .plus(
            listOf(
                ::eventsFromRespawnCountdowns
            )
                .flatMap { it(previous, world.deck, events) }
        )