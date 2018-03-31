package lab

import configuration.loadConfig
import configuration.saveConfig
import front.GameConfig
import front.loadGameConfig
import front.saveGameConfig
import generation.calculateWorldScale
import main.front.setWorldMesh
import generation.generateWorld
import generation.placeEnemies
import lab.views.GameViewConfig
import marloth.clienting.Client
import marloth.clienting.initialGameInputState
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import mythic.spatial.Vector3
import randomly.Dice
import simulation.World
import simulation.WorldBoundary
import simulation.WorldInput
import simulation.changing.WorldUpdater
import visualizing.createScenes
import kotlin.concurrent.thread
import simulation.changing.Instantiator
import simulation.changing.InstantiatorConfig
import simulation.createWorldBoundary

fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
    //    val gui = LabGui(setModelCode, getModelCode)
    LabGui.main(listOf())
  }
}

fun saveLabConfig(config: LabConfig) {
  saveConfig("labConfig.yaml", config)
}

fun createDice(config: GameViewConfig) =
    if (config.UseRandomSeed)
      Dice()
    else
      Dice(config.seed)

fun generateDefaultWorld(instantiatorConfig: InstantiatorConfig, gameViewConfig: GameViewConfig): World {
  val boundary = createWorldBoundary(gameViewConfig.worldLength)
  val dice = createDice(gameViewConfig)
  val world = generateWorld(WorldInput(
      boundary,
      dice
  ), instantiatorConfig)

  if (gameViewConfig.haveEnemies) {
    val scale = calculateWorldScale(boundary.dimensions)
    val instantiator = Instantiator(world, instantiatorConfig)
    placeEnemies(world, instantiator, dice, scale)
  }

  return world
}

data class LabApp(
    val platform: Platform,
    val config: LabConfig,
    val gameConfig: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val world: World = generateDefaultWorld(InstantiatorConfig(gameConfig.gameplay.defaultPlayerView), config.gameView),
    val client: Client = Client(platform),
    val labClient: LabClient = LabClient(config, client)
)

private var saveIncrement = 0

tailrec fun labLoop(app: LabApp, previousState: LabState) {
  app.display.swapBuffers()
  val scenes = createScenes(app.world, app.client.screens)
  val delta = app.timer.update().toFloat()
  val (commands, nextState) = app.labClient.update(scenes, app.world.meta, previousState, delta)
  val instantiator = Instantiator(app.world, InstantiatorConfig(app.gameConfig.gameplay.defaultPlayerView))
  val updater = WorldUpdater(app.world, instantiator)
  updater.update(commands, delta)
  app.platform.process.pollEvents()

  if (app.gameConfig.gameplay.defaultPlayerView != app.world.players[0].viewMode) {
    app.gameConfig.gameplay.defaultPlayerView = app.world.players[0].viewMode
    saveGameConfig(app.gameConfig)
  }
  if (saveIncrement++ > 60 * 3) {
    saveIncrement = 0
    saveLabConfig(app.config)
  }

  if (!app.platform.process.isClosing())
    labLoop(app, nextState)
}

fun runApp(platform: Platform, config: LabConfig, gameConfig: GameConfig) {
  platform.display.initialize(gameConfig.display)
  val app = LabApp(platform, config, gameConfig)
  setWorldMesh(app.world.meta, app.client)
  labLoop(app, LabState(mapOf(), initialGameInputState()))

}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val config = loadConfig<LabConfig>("labConfig.yaml") ?: LabConfig()
    val gameConfig = loadGameConfig()
    saveLabConfig(config)
//    startGui()
    runApp(createDesktopPlatform("Dev Lab"), config, gameConfig)
  }
}