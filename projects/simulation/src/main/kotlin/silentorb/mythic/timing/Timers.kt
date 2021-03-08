package silentorb.mythic.timing

import silentorb.mythic.ent.*

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
    val duration: Float,
    val original: Float = duration
)

fun updateFloatTimer(delta: Float): (FloatTimer) -> FloatTimer = { timer ->
  timer.copy(
      duration = timer.duration - delta
  )
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
