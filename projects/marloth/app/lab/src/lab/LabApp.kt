package lab

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
import com.fasterxml.jackson.databind.SequenceWriter
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import simulation.changing.Instantiator
import simulation.changing.InstantiatorConfig
import java.io.FileOutputStream


fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
    val gui = LabGui()
    gui.foo(listOf())
  }
}

fun loadLabConfig(path: String): LabConfig {
  if (File(path).isFile()) {
    val mapper = YAMLMapper()
    mapper.registerModule(KotlinModule())

    return Files.newBufferedReader(Paths.get(path)).use {
      mapper.readValue(it, LabConfig::class.java)
    }
  }

  return LabConfig()
}

fun saveLabConfig(path: String, config: LabConfig) {
  val mapper = YAMLMapper()
  mapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)
  mapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
  mapper.registerModule(KotlinModule())

  Files.newBufferedWriter(Paths.get(path)).use {
    mapper.writeValue(it, config)
  }
}

fun saveLabConfig(config: LabConfig) {
  saveLabConfig("labConfig.yaml", config)
}

data class LabApp(
    val platform: Platform,
    val config: LabConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val world: World = generateDefaultWorld(InstantiatorConfig(config.gameView.defaultPlayerView)),
    val client: Client = Client(platform),
    val labClient: LabClient = LabClient(config, client)
)

private var saveIncrement = 0

tailrec fun labLoop(app: LabApp, previousState: LabState) {
  app.display.swapBuffers()
  val scenes = createScenes(app.world, app.client.screens)
  val (commands, nextState) = app.labClient.update(scenes, app.world.meta, previousState)
  val delta = app.timer.update().toFloat()
  val instantiator = Instantiator(app.world, InstantiatorConfig(app.config.gameView.defaultPlayerView))
  val updater = WorldUpdater(app.world, instantiator)
  updater.update(commands, delta)
  app.platform.process.pollEvents()

  app.config.gameView.defaultPlayerView = app.world.players[0].viewMode
  if (saveIncrement++ > 60 * 3) {
    saveIncrement = 0
    saveLabConfig(app.config)
  }

  if (!app.platform.process.isClosing())
    labLoop(app, nextState)
}

fun runApp(platform: Platform, config: LabConfig) {
  val app = LabApp(platform, config)
  setWorldMesh(app.world.meta, app.client)
  labLoop(app, LabState(mapOf(), initialGameInputState()))

}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val config = loadLabConfig("labConfig.yaml")
    saveLabConfig(config)
    runApp(createDesktopPlatform("Dev Lab", config.width, config.height), config)
  }
}