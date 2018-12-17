package lab

import configuration.loadConfig
import front.loadGameConfig
import mythic.desktop.createDesktopPlatform

object WorldGenProfiler {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val config = loadConfig<LabConfig>(labConfigPath) ?: LabConfig()
    val gameConfig = loadGameConfig()
    val platform = createDesktopPlatform("WorldGenProfiler", gameConfig.display)
    platform.display.initialize(gameConfig.display)

    while(true) {
      platform.display.swapBuffers()
      platform.process.pollEvents()
      val world = generateDefaultWorld(config.gameView)
      Thread.sleep(50)
    }
  }
}