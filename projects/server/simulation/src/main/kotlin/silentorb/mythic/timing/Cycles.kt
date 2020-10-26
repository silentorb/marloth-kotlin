package silentorb.mythic.timing

data class FloatCycle(
    val speed: Float,
    val value: Float
)

fun updateFloatCycle(delta: Float): (FloatCycle) -> FloatCycle = { cycle ->
  cycle.copy(
      value = (cycle.value + cycle.speed * delta) % 1f
  )
}
