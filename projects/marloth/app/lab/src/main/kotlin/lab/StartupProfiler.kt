package lab

import configuration.ConfigManager
import marloth.front.newGameApp
import marloth.integration.loadGameConfig
import mythic.desktop.createDesktopPlatform

object StartupProfiler {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")

    while (true) {
      System.gc()
      val labConfig = loadLabConfig()
      val gameConfig = loadGameConfig()
      val platform = createDesktopPlatform("Dev Lab", gameConfig.display)
      platform.display.initialize(gameConfig.display)
      val gameApp = newGameApp(platform, gameConfig)
      val state = newLabState(gameApp, labConfig)
      val app = LabApp(gameApp, labConfig,
          labConfigManager = ConfigManager(labConfigPath, labConfig),
          labClient = LabClient(labConfig, gameApp.client)
      )
      gameApp.platform.process.pollEvents()
      if (gameApp.platform.process.isClosing()) {
        shutdownGameApp(gameApp)
        break
      }

      shutdownGameApp(gameApp)
    }
  }
}
