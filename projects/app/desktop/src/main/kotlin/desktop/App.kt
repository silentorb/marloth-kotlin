package desktop

import marloth.integration.front.runApp
import marloth.integration.misc.loadGameConfig
import silentorb.mythic.desktop.createDesktopPlatform
import silentorb.mythic.lookinglass.toPlatformDisplayConfig

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val gameConfig = loadGameConfig()
    val platformDisplayConfig = toPlatformDisplayConfig(gameConfig.display)
    val platform = createDesktopPlatform("Marloth", platformDisplayConfig)
    runApp(platform, gameConfig)
  }
}
