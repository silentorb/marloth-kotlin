package silentorb.mythic.performing

import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.AccessoryName
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

fun eventsFromPerformances(definitions: PerformanceDefinitions, deck: PerformanceDeck, delta: Float): Events {
  return deck.performances
      .flatMap { (id, performance) ->
        val timer = deck.timersFloat.getValue(id)
        val animation = definitions.animations.getValue(performance.animation)
        val elapsed = animation.duration - timer.duration
        performance.deferredEvents
            .mapNotNull { (markerName, event) ->
              val isTriggered = animation.markers.any {
                val frame = it.frame.toFloat() / 60f
                it.name == markerName && frame > elapsed && frame < elapsed + delta
              }
              if (isTriggered)
                event
              else
                null
            }
      }
}
