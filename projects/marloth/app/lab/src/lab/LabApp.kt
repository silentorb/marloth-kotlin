package lab

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import front.setWorldMesh
import generation.generateDefaultWorld
import marloth.clienting.Client
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
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

fun runApp(platform: Platform, config: LabConfig) {
  val display = platform.display
  val timer = DeltaTimer()
  val client = Client(platform)
  val world = generateDefaultWorld()
//  val world = createTestWorld()
  val labClient = LabClient(config, client)
  setWorldMesh(world.meta, client)

  while (!platform.process.isClosing()) {
    display.swapBuffers()
    val scene = createScene(world, client.screens[0])
    val commands = labClient.update(scene, world.meta)
    val delta = timer.update().toFloat()
    val updater = WorldUpdater(world)
    updater.update(commands, delta)
    platform.process.pollEvents()
  }
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    val config = loadLabConfig("labConfig.yaml")
    runApp(createDesktopPlatform("Dev Lab", config.width, config.height), config)
//    startGui()
//    runApp(createDesktopPlatform("Marloth Lab", 640, 480))
//    runApp(createDesktopPlatform("Marloth Lab", 320, 240))
  }
}