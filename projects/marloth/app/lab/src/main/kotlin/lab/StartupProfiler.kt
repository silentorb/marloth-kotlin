package lab

import configuration.ConfigManager
import marloth.front.GameApp
import marloth.integration.loadGameConfig
import mythic.desktop.createDesktopPlatform
import mythic.quartz.printProfiler
import physics.newBulletState

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
      val gameApp = GameApp(platform, gameConfig,
          bulletState = newBulletState()
      )
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