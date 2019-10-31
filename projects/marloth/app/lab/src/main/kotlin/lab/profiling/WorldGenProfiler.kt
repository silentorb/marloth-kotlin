package lab.profiling

import configuration.loadYamlFile
import lab.LabConfig
import lab.labConfigPath
import marloth.integration.loadGameConfig
import mythic.desktop.createDesktopPlatform

object WorldGenProfiler {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    val config = loadYamlFile<LabConfig>(labConfigPath) ?: LabConfig()
    val gameConfig = loadGameConfig()
    val platform = createDesktopPlatform("WorldGenProfiler", gameConfig.display)
    platform.display.initialize(gameConfig.display)

    while(true) {
      platform.display.swapBuffers()
      platform.process.pollEvents()
      throw Error("Needs to instantiate a client now to load static mesh collision shapes")
//      val world = generateRealm(config.gameView)
      Thread.sleep(50)
    }
  }
}
