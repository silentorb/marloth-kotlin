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

fun updateWorldAnimation(world: World, animation: Animation, delta: Float): World {
  val newAnimation = updateAnimation(animation, delta)
  if (newAnimation != null)
    return world.copy(animation = newAnimation)
  else
    return continueTurn(world, animation.action)
}

fun updateWorld(world: World?, command: GameCommand?, delta: Float): World? {
  return if (world != null) {
    if (command != null)
      startTurn(world, command.data as Action)
    else if (world.animation != null) {
      updateWorldAnimation(world, world.animation!!, delta)
    } else
      world
  } else
    null
}

tailrec fun appLoop(app: JunkApp, state: AppState) {
  app.display.swapBuffers()
  val delta = app.timer.update().toFloat()
  val (clientState, command) = app.client.update(state, delta)
  val newState = state.copy(client = clientState)
  val state2 = if (command != null && newState.world == null)
    updateOutsideOfWorld(newState, command)
  else if (!isGameOver(newState.world!!))
    newState.copy(world = updateWorld(newState.world, command, delta))
  else
    newState

  app.platform.process.pollEvents()

  if (!app.platform.process.isClosing())
    appLoop(app, state2)
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
    runApp(createDesktopPlatform("Dev Lab", gameConfig.display), gameConfig)
  }
}