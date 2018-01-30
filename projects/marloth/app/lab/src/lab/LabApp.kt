package lab

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import front.setWorldMesh
import generation.generateDefaultWorld
import haft.ProfileStates
import marloth.clienting.Client
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.World
import simulation.WorldUpdater
import visualizing.createScene
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread

fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
    val gui = LabGui()
    gui.foo(listOf())
  }
}

data class InputLabConfig(
    var showAbstract: Boolean = true,
    var showStructure: Boolean = true,
    var showLab: Boolean = false,
    var width: Int = 800,
    var height: Int = 600
)

fun loadLabConfig(path: String): LabConfig {
  val config = LabConfig()

  if (File(path).isFile()) {
    val mapper = ObjectMapper(YAMLFactory())
    mapper.registerModule(KotlinModule())

    val initial = Files.newBufferedReader(Paths.get(path)).use {
      mapper.readValue(it, InputLabConfig::class.java)
    }

    config.showAbstract = initial.showAbstract
    config.showStructure = initial.showStructure
    config.showLab = initial.showLab
    config.width = initial.width
    config.height = initial.height
  }

  return config
}

data class LabApp(
    val platform: Platform,
    val config: LabConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val world: World = generateDefaultWorld(),
    val client: Client = Client(platform),
    val labClient: LabClient = LabClient(config, client)
)

tailrec fun labLoop(app: LabApp, previousState: LabState) {
  app.display.swapBuffers()
  val scene = createScene(app.world, app.client.screens[0])
  val (commands, nextState) = app.labClient.update(scene, app.world.meta, previousState)
  val delta = app.timer.update().toFloat()
  val updater = WorldUpdater(app.world)
  updater.update(commands, delta)
  app.platform.process.pollEvents()

  if (!app.platform.process.isClosing())
    labLoop(app, nextState)
}

fun runApp(platform: Platform, config: LabConfig) {
  val app = LabApp(platform, config)
  setWorldMesh(app.world.meta, app.client)
  labLoop(app, LabState(mapOf(), mapOf()))

}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val config = loadLabConfig("labConfig.yaml")
    runApp(createDesktopPlatform("Dev Lab", config.width, config.height), config)
  }
}