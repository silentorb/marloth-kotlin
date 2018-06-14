package junk_app

import configuration.loadConfig
import junk_client.*
import junk_simulation.*
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

fun updateWorld(world: World?, command: GameCommand?, delta: Float): World? {
  if (world != null) {
    if (command != null)
      takeTurn(world, command.data as Action)
    else if (world.animation != null)
      world.copy(animation = updateAnimation(world.animation!!, delta))
  }

  return null
}

tailrec fun appLoop(app: JunkApp, state: AppState) {
  app.display.swapBuffers()
  val delta = app.timer.update().toFloat()
  val (clientState, command) = app.client.update(state, delta)
  val newState = state.copy(client = clientState)
  val state2 = if (command != null)
    updateOverlap(newState, command)
  else
    newState

  val state3 = state2.copy(world = updateWorld(state2.world, command, delta))

  app.platform.process.pollEvents()

  if (!app.platform.process.isClosing())
    appLoop(app, state3)
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