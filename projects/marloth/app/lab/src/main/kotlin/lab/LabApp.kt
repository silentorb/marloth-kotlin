package lab

import com.fasterxml.jackson.module.kotlin.KotlinModule
import configuration.loadConfig
import configuration.saveConfig
import front.GameConfig
import front.loadGameConfig
import front.saveGameConfig
import main.front.setWorldMesh
import generation.generateDefaultWorld
import marloth.clienting.Client
import marloth.clienting.initialGameInputState
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.World
import simulation.changing.WorldUpdater
import visualizing.createScenes
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread
import simulation.changing.Instantiator
import simulation.changing.InstantiatorConfig


fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
//    val gui = LabGui(setModelCode, getModelCode)
    LabGui.main(listOf())
  }
}

fun saveLabConfig(config: LabConfig) {
  saveConfig("labConfig.yaml", config)
}

data class LabApp(
    val platform: Platform,
    val config: LabConfig,
    val gameConfig: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val world: World = generateDefaultWorld(InstantiatorConfig(gameConfig.gameplay.defaultPlayerView)),
    val client: Client = Client(platform),
    val labClient: LabClient = LabClient(config, client)
)

private var saveIncrement = 0

tailrec fun labLoop(app: LabApp, previousState: LabState) {
  app.display.swapBuffers()
  val scenes = createScenes(app.world, app.client.screens)
  val (commands, nextState) = app.labClient.update(scenes, app.world.meta, previousState)
  val delta = app.timer.update().toFloat()
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