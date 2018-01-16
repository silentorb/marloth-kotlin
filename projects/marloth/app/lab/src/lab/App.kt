package lab

import marloth.clienting.Client
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import mythic.spatial.Vector4
import rendering.convertMesh
import serving.Server
import visualizing.createScene

fun runApp(platform: Platform) {
  val display = platform.display

  val timer = DeltaTimer()
  val server = Server()
  val client = Client(platform)
  val config = LabConfig()
  val marlothLab = MarlothLab(config)
  val labClient = LabClient(config, client)
  client.renderer.worldMesh = convertMesh(marlothLab.structureWorld.mesh, client.renderer.vertexSchemas.standard,
      Vector4(0.5f, 0.2f, 0f, 1f))

  while (!platform.process.isClosing()) {
    display.swapBuffers()
    val scene = createScene(server.world, client.screens[0])
    val commands = labClient.update(scene, marlothLab)
    val delta = timer.update().toFloat()
    server.update(commands, delta)
    platform.process.pollEvents()
  }
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    runApp(createDesktopPlatform("Marloth Lab", 1200, 800))
  }
}