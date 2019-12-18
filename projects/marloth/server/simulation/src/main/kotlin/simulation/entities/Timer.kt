package simulation.entities

import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Events
import simulation.main.Deck

data class Timer(
    val duration: Int,
    val interval: Int
)

val updateTimer: (Timer) -> Timer = { timer ->
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

fun updateIntTimers(events: Events): (Table<Timer>) -> Table<Timer> = { timers ->
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

//    if (isIntTimerUpdateFrame(events)) mapTableValues(updateTimer) else ::pass

fun expiredTimers(deck: Deck): Set<Id> =
    setOf<Id>()
        .plus(
            deck.timers
                .filter { it.value.duration < 0 }
                .keys
        )
        .plus(
            deck.timersFloat
                .filter { it.value.duration < 0f }
                .keys
        )
