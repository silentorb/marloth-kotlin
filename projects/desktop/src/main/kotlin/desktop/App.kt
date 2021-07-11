package desktop

import marloth.clienting.loadGameConfig
import marloth.integration.front.runApp
import silentorb.mythic.desktop.createDesktopPlatform
import silentorb.mythic.logging.logger
import silentorb.mythic.lookinglass.toPlatformDisplayConfig

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val gameConfig = loadGameConfig()
    val platformDisplayConfig = toPlatformDisplayConfig(gameConfig.display)
    val platform = createDesktopPlatform("Marloth", platformDisplayConfig)
    try {
      runApp(platform, gameConfig)
    } catch (error: Throwable) {
      logger.error("Game closed with the following error", error)
      platform.process.messageBox("Oh No!", "There was a problem and the game is dead!")
    }
  }
}
