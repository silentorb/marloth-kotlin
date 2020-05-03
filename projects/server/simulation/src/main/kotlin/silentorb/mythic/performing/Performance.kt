package silentorb.mythic.performing

import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.breeze.AnimationInfo
import silentorb.mythic.breeze.AnimationInfoMap
import silentorb.mythic.breeze.AnimationName
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.happenings.UseAction
import silentorb.mythic.timing.FloatTimer

data class PerformanceDeck(
    val accessories: Table<Accessory>,
    val performances: Table<Performance>,
    val timersFloat: Table<FloatTimer>
)

data class PerformanceDefinitions(
    val actions: Map<AccessoryName, ActionDefinition>,
    val animations: AnimationInfoMap
)

data class Performance(
    val target: Id,
    val animation: AnimationName,
    val sourceAction: Id? = null,
    val deferredEvents: Map<String, GameEvent> = mapOf()
)

fun performancesFromEvents(definitions: PerformanceDefinitions, deck: PerformanceDeck, events: Events): List<Performance> {
  val actionEvents = events.filterIsInstance<UseAction>()
  return actionEvents.mapNotNull { event ->
    val accessory = deck.accessories[event.action]
    val definition = definitions.actions[accessory?.type]
    val animation = definition?.animation
    if (animation != null) {
      Performance(
          target = event.actor,
          animation = animation,
          sourceAction = event.action,
          deferredEvents = event.deferredEvents
      )
    } else
      null
  }
}

fun isMarkerTriggered(duration: Float, animation: AnimationInfo, delta: Float): (String) -> Boolean = { markerName ->
  val elapsed = animation.duration - duration
  animation.markers.any {
    val frame = it.frame.toFloat() / 60f
    it.name == markerName && frame > elapsed && frame < elapsed + delta
  }
}

fun eventsFromPerformances(definitions: PerformanceDefinitions, deck: PerformanceDeck, delta: Float): Events {
  return deck.performances
      .flatMap { (id, performance) ->
        val animation = definitions.animations.getValue(performance.animation)
        val timer = deck.timersFloat.getValue(id)
        performance.deferredEvents
            .filterKeys(isMarkerTriggered(timer.duration, animation, delta))
            .values
      }
}

fun isPerforming(performances: Table<Performance>, actor: Id): Boolean =
    performances.any { it.value.target == actor }
