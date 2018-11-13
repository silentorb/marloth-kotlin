package gctest

import mythic.desktop.createDesktopPlatform
import mythic.glowing.Glow
import mythic.platforming.PlatformDisplayConfig
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer

data class GCTestApp(
    val platform: Platform,
    val glow: Glow,
    val timer: DeltaTimer = DeltaTimer()
)

const val Second: Long = 1000L
const val nSecond: Long = 1000000000L
const val nHalfMinute: Long = 30 * nSecond

const val maxInterval = 1f / 60f

fun basicLoop(app: GCTestApp, onLoop: () -> Unit = {}) {
  do {
    app.platform.display.swapBuffers()
    app.glow.operations.clearScreen()
    val delta = app.timer.update().toFloat()
    val progress = app.timer.last - app.timer.start
    if (app.timer.actualDelta > maxInterval) {
      println("" + (progress.toDouble() / nSecond.toDouble()).toFloat() + ": " + app.timer.actualDelta)
    }
    app.platform.process.pollEvents()
    onLoop()
  } while (!app.platform.process.isClosing()
//      && progress < nHalfMinute
  )
}

fun heavyLoop(app: GCTestApp) {
  val workload = Workload(true)
  val creator = { i: Int -> createA(i) }
  basicLoop(app) {
    workload.update(creator)
  }

  // Just making sure code isn't optimized away.
  if (workload.useBList)
    println(workload.blist.last().a)

  println(workload.unusedResult)
}

//fun runApp(platform: Platform) {
//  platform.display.initialize(DisplayConfig(1400, 900, vsync = false))
//  val glow = Glow()
//  val app = GCTestApp(platform, glow)
//  if (true)
//    heavyLoop(app)
//  else
//    basicLoop(app)
//}
//
//object App {
//  @JvmStatic
//  fun main(args: Array<String>) {
//    runApp(createDesktopPlatform("GC Test", PlatformDisplayConfig()))
//  }
//}