package simulation.entities

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

data class IntCycle(
    val interval: Int,
    val value: Int
)

val updateIntCycle: (IntCycle) -> IntCycle = { cycle ->
  cycle.copy(
      value = (cycle.value + 1) % cycle.interval
  )
}

data class IntCycleEvent(
    val interval: Int
) : GameEvent

fun emitCycleEvents(cycles: Table<IntCycle>): Events =
    cycles.values.filter { it.value == 0 }
        .map { IntCycleEvent(interval = it.interval) }
