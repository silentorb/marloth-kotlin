package front

import generation.generateDefaultWorld
import marloth.clienting.Client
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.updateWorld
import visualizing.createScene

fun runApp(platform: Platform) {
  val display = platform.display
  val timer = DeltaTimer()
  val world = generateDefaultWorld()
  val client = Client(platform)
  setWorldMesh(world.meta, client)

  while (!platform.process.isClosing()) {
    display.swapBuffers()
    val scene = createScene(world, client.screens[0])
    val commands = client.update(scene)
    val delta = timer.update().toFloat()
    updateWorld(world, commands, delta)
    platform.process.pollEvents()
  }
}
