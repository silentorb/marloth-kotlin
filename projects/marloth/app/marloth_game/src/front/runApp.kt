package front

import marloth.clienting.Client
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import serving.Server
import visualizing.createScene

fun is64Bit(): Boolean {
  if (System.getProperty("os.name").contains("Windows")) {
    return System.getenv("ProgramFiles(x86)") != null;
  } else {
    return System.getProperty("os.arch").indexOf("64") != -1;
  }
}

fun runApp(platform: Platform) {
  val display = platform.display

  val timer = DeltaTimer()
  val server = Server()
  val client = Client(platform)

  while (!platform.process.isClosing()) {
    display.swapBuffers()
    val scene = createScene(server.world, client.screens[0])
    val commands = client.update(scene)
    val delta = timer.update().toFloat()
    server.update(commands, delta)
    platform.process.pollEvents()
  }
}