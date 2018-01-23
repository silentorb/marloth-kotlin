package lab

import front.setWorldMesh
import generation.generateDefaultWorld
import marloth.clienting.Client
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.*
import visualizing.createScene
import kotlin.concurrent.thread

fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
    val gui = LabGui()
    gui.foo(listOf())
  }
}

fun runApp(platform: Platform) {
  val display = platform.display
  val timer = DeltaTimer()
  val client = Client(platform)
  val config = LabConfig()
  val world = generateDefaultWorld()
  val labClient = LabClient(config, client)
  setWorldMesh(world.meta, client)

  while (!platform.process.isClosing()) {
    display.swapBuffers()
    val scene = createScene(world, client.screens[0])
    val commands = labClient.update(scene, world.meta)
    val delta = timer.update().toFloat()
    updateWorld(world, commands, delta)
    platform.process.pollEvents()
  }
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    runApp(createDesktopPlatform("Marloth Lab", 1200, 800))
//    startGui()
//    runApp(createDesktopPlatform("Marloth Lab", 640, 480))
//    runApp(createDesktopPlatform("Marloth Lab", 320, 240))
  }
}