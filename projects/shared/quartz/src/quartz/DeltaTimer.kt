package quartz

class DeltaTimer {
  private val start = System.nanoTime() // Just used for reference
  private var last = start

  fun update():Double {
    val now = System.nanoTime()
    val gap = now - last
    val result = gap.toDouble() / 1000000000 // 1,000,000,000
    last = now
    return result
  }
}