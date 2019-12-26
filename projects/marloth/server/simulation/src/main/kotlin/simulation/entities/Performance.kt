package simulation.entities

import silentorb.mythic.ent.Id
import marloth.scenery.ArmatureId
import silentorb.mythic.ent.IdSource
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.UseAction
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.Definitions

data class Performance(
    val target: Id,
    val animation: AnimationName
)

fun newPerformanceHand(animationDurations: AnimationDurationMap, nextId: IdSource): (Performance) -> IdHand = { performance ->
  IdHand(
      id = nextId(),
      hand = Hand(
          performance = performance,
          timerFloat = FloatTimer(
              duration = animationDurations[ArmatureId.person.name]!![performance.animation]!!
          )
      )
  )
}

fun performancesFromEvents(definitions: Definitions, animationDurations: AnimationDurationMap, nextId: IdSource, deck: Deck, events: Events): List<IdHand> {
  val actionEvents = events.filterIsInstance<UseAction>()
  return actionEvents.mapNotNull { event ->
    val accessory = deck.accessories[event.action]
    val definition = definitions.actions[accessory?.type]
    val animation = definition?.animation
    if (animation != null) {
      val performance = Performance(
          target = event.actor,
          animation = animation
      )
      newPerformanceHand(animationDurations, nextId)(performance)
    } else
      null
  }
}
