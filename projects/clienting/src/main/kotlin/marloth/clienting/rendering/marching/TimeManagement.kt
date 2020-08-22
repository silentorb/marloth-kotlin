package marloth.clienting.rendering.marching

typealias ServiceTimeMeasurements = Map<String, Long>

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

fun isGateReached(gate: TimeGate): Boolean =
    gate.start + getNanoTime() >= gate.absoluteLimit

fun <T>measureTime(action: () -> T): Pair<T, Long>{
  val start = getNanoTime()
  val result = action()
  val duration = getNanoTime() - start
  return result to duration
}
