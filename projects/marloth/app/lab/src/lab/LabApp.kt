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

fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
    val gui = LabGui()
    gui.foo(listOf())
  }
}

//data class InputLabConfig(
//    @field:JsonIgnore
//    var view: LabView = LabView.world,
////    @field:JsonIgnore
////    var k:Int = 0,
//    var showAbstract: Boolean = true,
//    var showStructure: Boolean = true,
//    var showLab: Boolean = false,
//    var width: Int = 800,
//    var height: Int = 600
//)

//fun loadLabConfig2(path: String): InputLabConfig {
//  val temp = InputLabConfig()
//  val props = temp.javaClass.fields
//  val fields = temp.javaClass.kotlin.members
//  if (File(path).isFile()) {
//    val mapper = ObjectMapper(YAMLFactory())
//    mapper.registerModule(KotlinModule())
//
//    return Files.newBufferedReader(Paths.get(path)).use {
//      mapper.readValue(it, InputLabConfig::class.java)
//    }
//  }
//
//  return InputLabConfig()
//}

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
  val scenes = createScenes(app.world, app.client.screens)
  val (commands, nextState) = app.labClient.update(scenes, app.world.meta, previousState)
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
  labLoop(app, LabState(mapOf(), initialGameInputState()))

}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val config = loadLabConfig("labConfig.yaml")
    runApp(createDesktopPlatform("Dev Lab", config.width, config.height), config)
  }
}