package marloth.clienting.rendering.marching

typealias ServiceTimes = Map<String, Long>

data class TimeGate(
    val start: Long,
    val absoluteLimit: Long
)

fun getNanoTime(): Long =
    System.nanoTime()

fun newTimeGate(relativeLimit: Long): TimeGate {
  val start = getNanoTime()
  return TimeGate(
      start = start,
      absoluteLimit = start + relativeLimit
  )
}

fun isGateReached(gate: TimeGate): Boolean {
  val now = getNanoTime()
  return now >= gate.absoluteLimit
}

fun <T> measureTime(action: () -> T): Pair<T, Long> {
  val start = getNanoTime()
  val result = action()
  val duration = getNanoTime() - start
  return result to duration
}

fun calculateTimePool(idle: Long, fixedBuffer: Long, consumption: Long): Long =
    consumption + idle - fixedBuffer

fun allocateTimeLimits(idle: Long, fixedBuffer: Long, measurements: ServiceTimes): ServiceTimes {
  return if (measurements.none())
    mapOf()
  else {
    val previousTotal = measurements.values.sum()
    val pool = calculateTimePool(idle, fixedBuffer, previousTotal)
    val average = previousTotal / measurements.size
    measurements.mapValues { it.value * pool / average }
  }
}
