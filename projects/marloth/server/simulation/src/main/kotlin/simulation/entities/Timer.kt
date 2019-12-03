package simulation.entities

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
