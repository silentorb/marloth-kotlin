package desktop

import marloth.clienting.loadGameConfig
import marloth.integration.front.newGameApp
import marloth.integration.front.runApp
import marloth.integration.misc.newWorld
import silentorb.mythic.desktop.createDesktopPlatform
import silentorb.mythic.logging.logger
import silentorb.mythic.lookinglass.toPlatformDisplayConfig
import silentorb.mythic.physics.releaseBulletState

object ProfileWorldGeneration {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    configureLogging()
    val gameConfig = loadGameConfig()
    val platformDisplayConfig = toPlatformDisplayConfig(gameConfig.display)
    val platform = createDesktopPlatform("World Generation Profiling", platformDisplayConfig)
    val app = newGameApp(platform, gameConfig)
    while (!app.platform.process.isClosing()) {
      app.platform.process.pollEvents()
      val world = newWorld(app)
      releaseBulletState(world.bulletState)
    }
  }
}
