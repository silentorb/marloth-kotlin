package desktop

import marloth.integration.loadGameConfig
import marloth.front.runApp
import silentorb.mythic.desktop.createDesktopPlatform

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val gameConfig = loadGameConfig()
    val platform = createDesktopPlatform("Marloth", gameConfig.display)
    runApp(platform, gameConfig)
  }
}
