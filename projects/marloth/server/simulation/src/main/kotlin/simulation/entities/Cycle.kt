package simulation.entities

data class Cycle(
    val speed: Float,
    val value: Float
)

fun updateCycle(delta: Float): (Cycle) -> Cycle = { cycle ->
  cycle.copy(
      value = (cycle.value + cycle.speed * delta) % 1f
  )
}
