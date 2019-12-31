package simulation.updating

import silentorb.mythic.audio.soundsFromEvents
import silentorb.mythic.aura.Sound
import silentorb.mythic.breeze.AnimationInfoMap
import simulation.entities.applyBuffsFromEvents
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.toIdHands
import silentorb.mythic.happenings.Events
import silentorb.mythic.performing.Performance
import silentorb.mythic.performing.performancesFromEvents
import silentorb.mythic.timing.FloatTimer
import simulation.entities.*
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

fun newEntities(definitions: Definitions, previous: Deck, events: Events, nextId: IdSource): (Deck) -> Deck = { next ->
  val idHands = listOf(
      newRespawnCountdowns(nextId, previous, next),
      newPerformances(definitions, previous, events, nextId),
      toIdHands(nextId, newAmbientSounds(previous, next)),
      toIdHands(nextId, handsFromSounds(soundsFromEvents(events)))
  )
      .flatten()

  val additions = listOf(
      newAccessoriesDeck(events, previous)
  )
      .plus(idHandsToDeck(idHands))
      .plus(applyBuffsFromEvents(previous, nextId, events))

  listOf(next).plus(additions).reduce(mergeDecks)
}
