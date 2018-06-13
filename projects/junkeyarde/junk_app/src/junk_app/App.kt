package junk_app

import configuration.loadConfig
import junk_client.*
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer

data class JunkApp(
    val platform: Platform,
    val gameConfig: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val client: Client = Client(platform)
)

tailrec fun appLoop(app: JunkApp, state: AppState) {
  app.display.swapBuffers()
  val delta = app.timer.update().toFloat()
  val (clientState, command) = app.client.update(state, delta)
  val newState = state.copy(client = clientState)
  val newState2 = if (command != null)
    updateOverlap(newState, command)
  else
    newState

  app.platform.process.pollEvents()

  if (!app.platform.process.isClosing())
    appLoop(app, newState2)
}

fun runApp(platform: Platform, gameConfig: GameConfig) {
  platform.display.initialize(gameConfig.display)
  val app = JunkApp(platform, gameConfig)
  val labConfig = loadConfig<LabConfig>("junkLabConfig.yaml")
  val state = if (labConfig != null)
    newLabAppState(labConfig)
  else
    newAppState()

  appLoop(app, state)
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val gameConfig = loadGameConfig()
    runApp(createDesktopPlatform("Dev Lab"), gameConfig)
  }
}