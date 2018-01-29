package lab

import com.fasterxml.jackson.annotation.JsonIgnore
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
    @field:JsonIgnore
    var view: LabView = LabView.world,
//    @field:JsonIgnore
//    var k:Int = 0,
    var showAbstract: Boolean = true,
    var showStructure: Boolean = true,
    var showLab: Boolean = false,
    var width: Int = 800,
    var height: Int = 600
)

fun loadLabConfig2(path: String): InputLabConfig {
  val temp = InputLabConfig()
  val props = temp.javaClass.fields
  val fields = temp.javaClass.kotlin.members
  if (File(path).isFile()) {
    val mapper = ObjectMapper(YAMLFactory())
    mapper.registerModule(KotlinModule())

    return Files.newBufferedReader(Paths.get(path)).use {
      mapper.readValue(it, InputLabConfig::class.java)
    }
  }

  return InputLabConfig()
}

fun loadLabConfig(path: String): LabConfig {
  if (File(path).isFile()) {
    val mapper = ObjectMapper(YAMLFactory())
    mapper.registerModule(KotlinModule())

    return Files.newBufferedReader(Paths.get(path)).use {
      mapper.readValue(it, LabConfig::class.java)
    }
  }

  return LabConfig()
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
    System.setProperty("joml.format", "false")
    val config2 = loadLabConfig2("labConfig.yaml")

    val config = loadLabConfig("labConfig.yaml")
    runApp(createDesktopPlatform("Dev Lab", config.width, config.height), config)
//    startGui()
//    runApp(createDesktopPlatform("Marloth Lab", 640, 480))
//    runApp(createDesktopPlatform("Marloth Lab", 320, 240))
  }
}