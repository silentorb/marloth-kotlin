package silentorb.mythic.timing

import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Events

data class IntTimer(
    val duration: Int,
    val interval: Int
)

val updateTimer: (IntTimer) -> IntTimer = { timer ->
  timer.copy(
      duration = timer.duration - 1
  )
}

data class FloatTimer(
    val duration: Float
)

fun updateFloatTimer(delta: Float): (FloatTimer) -> FloatTimer = { timer ->
  timer.copy(
      duration = timer.duration - delta
  )
}

fun updateIntTimers(events: Events): (Table<IntTimer>) -> Table<IntTimer> = { timers ->
  val frequencies = events
      .filterIsInstance<IntCycleEvent>()
      .map { it.interval }
      .distinct()

  timers.mapValues { (_, timer) ->
    if (frequencies.contains(timer.interval))
      updateTimer(timer)
    else
      timer
  }
}

fun expiredTimers(timersFloat: Table<FloatTimer>, timersInt: Table<IntTimer>): Set<Id> =
    setOf<Id>()
        .plus(
            timersFloat
                .filter { it.value.duration < 0f }
                .keys
        )
        .plus(
            timersInt
                .filter { it.value.duration < 0 }
                .keys
        )
