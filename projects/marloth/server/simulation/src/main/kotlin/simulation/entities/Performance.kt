package simulation.entities

import mythic.ent.Id
import marloth.scenery.ArmatureId
import simulation.happenings.Events
import simulation.happenings.UseAction
import simulation.main.Deck
import simulation.main.Hand
import simulation.misc.Definitions

data class Performance(
    val target: Id,
    val animation: AnimationName
)

fun newPerformanceHand(animationDurations: AnimationDurationMap): (Performance) -> Hand = { performance ->
  Hand(
      performance = performance,
      timerFloat = FloatTimer(
          duration = animationDurations[ArmatureId.person.name]!![performance.animation]!!
      )
  )
}

fun performancesFromEvents(definitions: Definitions, animationDurations: AnimationDurationMap, deck: Deck, events: Events): List<Hand> {
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
      newPerformanceHand(animationDurations)(performance)
    } else
      null
  }
}
