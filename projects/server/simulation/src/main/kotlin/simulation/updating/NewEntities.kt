package simulation.updating

import silentorb.mythic.audio.soundsFromEvents
import silentorb.mythic.breeze.AnimationInfoMap
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.toIdHands
import silentorb.mythic.happenings.Events
import silentorb.mythic.performing.Performance
import silentorb.mythic.performing.performancesFromEvents
import silentorb.mythic.timing.FloatTimer
import simulation.abilities.newEntangleEntities
import simulation.characters.newPlayerCharacters
import simulation.combat.newDamageVisualEffects
import simulation.entities.*
import simulation.happenings.NewHandEvent
import simulation.entities.pruningEventsToIdHands
import simulation.main.*
import simulation.misc.*

fun newAccessoriesDeck(events: Events, deck: Deck): Deck =
    Deck(
        accessories = newAccessories(events, deck)
    )

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

fun newEntities(definitions: Definitions, grid: MapGrid, previous: Deck, events: Events, nextId: IdSource): (Deck) -> Deck = { next ->
  val idHands = listOf(
      newRespawnCountdowns(nextId, previous, next),
      newPerformances(definitions, previous, events, nextId),
      listOf(
          newAmbientSounds(previous, next),
          handsFromSounds(soundsFromEvents(events)),
          events.filterIsInstance<NewHandEvent>().map { it.hand },
          newDamageVisualEffects(next, events),
          newEntangleEntities(previous)
      )
          .flatMap { toIdHands(nextId, it) }
  )
      .flatten()
      .plus(pruningEventsToIdHands(events))
      .plus(placeVictoryKeys(grid, next, events))
      .plus(newPlayerCharacters(nextId, definitions, grid, events))

  val additions = listOf(
      newAccessoriesDeck(events, previous)
  )
      .plus(idHandsToDeck(idHands))

  listOf(next).plus(additions).reduce(mergeDecks)
}
