package gctest

import mythic.desktop.createDesktopPlatform
import mythic.platforming.DisplayConfig
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer

data class GCApp(
    val platform: Platform,
    val timer: DeltaTimer = DeltaTimer()
)

fun labLoop(app: GCApp) {
  val workload = Workload()
  do {
    app.platform.display.swapBuffers()
//    val delta = app.timer.update().toFloat()
    app.platform.process.pollEvents()
    workload.update()
  } while (!app.platform.process.isClosing())

  // Just making sure code isn't optimized away.
  println(workload.unusedResult)
}

fun runApp(platform: Platform) {
  platform.display.initialize(DisplayConfig(1400, 900, false, false))
  val app = GCApp(platform)
  labLoop(app)
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    runApp(createDesktopPlatform("GC Test"))
  }
}