package mythic.quartz

class Profiler {
  var lastName: String = ""
  var lastStart: Long = 0

  val metrics: MutableMap<String, Long> = mutableMapOf()

  fun start(name: String) {
    if (lastStart != 0L) {
      stop()
    }
    lastStart = System.nanoTime()
    lastName = name
  }

  fun stop() {
    metrics[lastName] = System.nanoTime() - lastStart
    lastStart = 0L
  }

  fun <T> wrap(name: String, action: () -> T): T {
    start(name)
    val result = action()
    stop()
    return result
  }
}

fun printProfiler(profiler: Profiler) {
  for (metric in profiler.metrics) {
    println(
        metric.key.padStart(12, ' ')
            + " "
            + String.format("%,d", metric.value).padStart(24, ' ')
    )
  }
}

private var profiler: Profiler? = null

fun globalProfiler(): Profiler {
  if (profiler == null)
    profiler = Profiler()

  return profiler!!
}

