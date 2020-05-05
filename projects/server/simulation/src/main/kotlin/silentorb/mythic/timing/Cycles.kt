package silentorb.mythic.timing

import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent

data class FloatCycle(
    val speed: Float,
    val value: Float
)

fun updateFloatCycle(delta: Float): (FloatCycle) -> FloatCycle = { cycle ->
  cycle.copy(
      value = (cycle.value + cycle.speed * delta) % 1f
  )
}
