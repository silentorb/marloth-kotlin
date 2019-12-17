package simulation.entities

import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import simulation.main.Deck

data class Timer(
    val duration: Int
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

val isIntTimerUpdateFrame: (Events) -> Boolean = singleValueCache { events ->
  events.any { it is UpdateIntTimersEvent }
}

fun updateIntTimers(events: Events): (Table<Timer>) -> Table<Timer> =
    if (isIntTimerUpdateFrame(events)) mapTableValues(updateTimer) else ::pass

class UpdateIntTimersEvent : GameEvent

val updateIntTimersEvent = UpdateIntTimersEvent()

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
