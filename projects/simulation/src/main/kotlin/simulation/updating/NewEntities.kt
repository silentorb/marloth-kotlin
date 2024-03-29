package simulation.updating

import silentorb.mythic.audio.soundsFromEvents
import silentorb.mythic.breeze.AnimationInfoMap
import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.performing.Performance
import silentorb.mythic.performing.performancesFromEvents
import silentorb.mythic.timing.FloatTimer
import simulation.abilities.newEntangleEntities
import simulation.accessorize.Accessory
import simulation.characters.newPlayerCharacters
import simulation.combat.newDamageVisualEffects
import simulation.entities.pruningEventsToIdHands
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

fun incorporateNewAccessories(definitions: Definitions, previous: Table<Accessory>, additions: Table<Accessory>): Table<Accessory> {
  val filteredAdditions = additions.filter { (id, accessory) ->
    val definition = definitions.accessories[accessory.type]
    !(!(definition?.many ?: true) && previous.any {
      it.value.owner == accessory.owner && it.value.type == accessory.type
    })
  }
  return previous + filteredAdditions
}

fun newEntities(definitions: Definitions, graph: Graph, step: Long, previous: Deck, events: Events, nextId: IdSource): (Deck) -> Deck = { next ->
  val commands = events.filterIsInstance<Command>()
  val idHands = listOf(
//      newRespawnCountdowns(nextId, previous, next),
      newPerformances(definitions, previous, events, nextId),
      listOf(
          newAmbientSounds(previous, next),
          handsFromSounds(soundsFromEvents(events)),
//          newChosenAccessories(previous.accessories, events)
      )
          .flatMap { toIdHands(nextId, it) }
  )
      .flatten()
      .plus(pruningEventsToIdHands(events))

  val lastDeck = listOf(next)
      .plus(idHandsToDeck(idHands))
      .reduce(mergeDecks)

  val newHands = newPlayerCharacters(nextId, definitions, graph, events) +
      newDamageVisualEffects(next, events) +
      newAccessories(events, definitions) +
      newEntangleEntities(previous) +
      events.filterIsInstance<NewHand>() +
      getNewHandsFromCommands(commands)

  allHandsToDeck(definitions, nextId, newHands, lastDeck, step)
}
