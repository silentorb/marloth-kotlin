package compuquest_app

import compuquest_client.*
import compuquest_simulation.*
import silentorb.mythic.configuration.loadYamlFile
import silentorb.mythic.desktop.createDesktopPlatform
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.PlatformDisplay
import silentorb.mythic.quartz.DeltaTimer
import silentorb.mythic.spatial.Vector2i

data class CompuQuestApp(
    val platform: Platform,
    val gameConfig: GameConfig,
    val display: PlatformDisplay = platform.display,
    val timer: DeltaTimer = DeltaTimer()
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

tailrec fun appLoop(app: CompuQuestApp, state: AppState) {
  app.display.swapBuffers()
  val delta = app.timer.update().toFloat()
  val (clientState, command) = updateClient(app.platform, state, delta)
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

const val labConfigPath = "../labConfig.yaml"

fun loadLabConfig(): LabConfig? =
    loadYamlFile<LabConfig>(labConfigPath)

private val renderLowSize = Vector2i(800, 600)

fun runApp(platform: Platform, gameConfig: GameConfig) {
  platform.display.initialize(gameConfig.display)
  val app = CompuQuestApp(platform, gameConfig)
  val labConfig = loadLabConfig()
  val state = if (labConfig != null)
    newLabAppState(labConfig, renderLowSize)
  else
    newAppState(renderLowSize)

  appLoop(app, state)
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val gameConfig = loadGameConfig()
    runApp(createDesktopPlatform("CompuQuest", gameConfig.display), gameConfig)
  }
}
