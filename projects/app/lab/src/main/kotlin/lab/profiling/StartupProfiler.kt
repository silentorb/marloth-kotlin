package lab.profiling

import silentorb.mythic.configuration.ConfigManager
import lab.*
import marloth.integration.front.newGameApp
import marloth.integration.misc.loadGameConfig
import silentorb.mythic.desktop.createDesktopPlatform
import silentorb.mythic.lookinglass.toPlatformDisplayConfig

object StartupProfiler {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")

    while (true) {
      System.gc()
      val labConfig = loadLabConfig()
      val gameConfig = loadGameConfig()
      val platformDisplayConfig = toPlatformDisplayConfig(gameConfig.display)
      val platform = createDesktopPlatform("Dev Lab", platformDisplayConfig)
      platform.display.initialize(platformDisplayConfig)
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
