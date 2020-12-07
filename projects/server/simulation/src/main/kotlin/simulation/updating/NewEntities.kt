package simulation.updating

import silentorb.mythic.audio.soundsFromEvents
import silentorb.mythic.breeze.AnimationInfoMap
import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Events
import silentorb.mythic.performing.Performance
import silentorb.mythic.performing.performancesFromEvents
import silentorb.mythic.timing.FloatTimer
import simulation.abilities.newEntangleEntities
import simulation.accessorize.newChosenAccessories
import simulation.characters.newPlayerCharacters
import simulation.combat.newDamageVisualEffects
import simulation.entities.newRespawnCountdowns
import simulation.entities.pruningEventsToIdHands
import simulation.happenings.NewHandEvent
import simulation.main.*
import simulation.misc.*

fun newPerformanceHand(animations: AnimationInfoMap, nextId: IdSource): (Performance) -> IdHand = { performance ->
  IdHand(
      id = nextId(),
      hand = Hand(
          performance = performance,
          timerFloat = FloatTimer(
              duration = animations[performance.animation]!!.duration
          )
      )
  )
}

fun newPerformances(definitions: Definitions, previous: Deck, events: Events, nextId: IdSource): List<IdHand> {
  val performanceDefinitions = toPerformanceDefinitions(definitions)
  val performanceDeck = toPerformanceDeck(previous)
  return performancesFromEvents(performanceDefinitions, performanceDeck, events)
      .map(newPerformanceHand(definitions.animations, nextId))
}

fun finalizeHands(nextId: IdSource, additions: List<NewHand> = listOf()): (NewHand) -> List<SimpleHand> = { hand ->
  listOf(
      SimpleHand(
          id = hand.id ?: nextId(),
          components = hand.components + additions,
      )
  ) + hand.children.flatMap(finalizeHands(nextId, listOf()))
}

inline fun <reified T> applyHands(hands: List<SimpleHand>): Table<T> =
    hands.mapNotNull { hand ->
      val component = hand.components
          .filterIsInstance<T>()
          .firstOrNull()
      if (component != null)
        hand.id to component
      else
        null
    }
        .associate { it }

fun newEntities(definitions: Definitions, graph: Graph, previous: Deck, events: Events, nextId: IdSource): (Deck) -> Deck = { next ->
  val idHands = listOf(
      newRespawnCountdowns(nextId, previous, next),
      newPerformances(definitions, previous, events, nextId),
      listOf(
          newAmbientSounds(previous, next),
          handsFromSounds(soundsFromEvents(events)),
          events.filterIsInstance<NewHandEvent>().map { it.hand },
          newDamageVisualEffects(next, events),
          newEntangleEntities(previous),
          newChosenAccessories(previous.accessories, events)
      )
          .flatMap { toIdHands(nextId, it) }
  )
      .flatten()
      .plus(pruningEventsToIdHands(events))

  val lastDeck = listOf(next)
      .plus(idHandsToDeck(idHands))
      .reduce(mergeDecks)

  val newHands = newPlayerCharacters(nextId, definitions, graph, events) +
      newAccessories(events, definitions)

  allHandsToDeck(nextId, newHands, lastDeck)
}
