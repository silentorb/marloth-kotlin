package junk_app

import junk_client.AppState
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import junk_client.Client
import junk_client.newAppState

data class JunkApp(
    val platform: Platform,
    val gameConfig: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val client: Client = Client(platform)
)

tailrec fun appLoop(app: JunkApp, state: AppState) {
  app.display.swapBuffers()
//  val scenes = createScenes(app.world, app.client.screens)
  val delta = app.timer.update().toFloat()
//  val (commands, nextState, menuAction) = app.labClient.update(scenes, app.world.meta, state, delta)
//  if (app.config.view == Views.game) {
//    val instantiator = Instantiator(app.world, InstantiatorConfig(app.gameConfig.gameplay.defaultPlayerView))
//    val updater = WorldUpdater(app.world, instantiator)
//    updater.update(commands, delta)
//  }

  val newState = state.copy(client = app.client.update(state, delta))
  app.platform.process.pollEvents()

  if (!app.platform.process.isClosing())
    appLoop(app, newState)
}

fun runApp(platform: Platform, gameConfig: GameConfig) {
  platform.display.initialize(gameConfig.display)
  val app = JunkApp(platform, gameConfig)
  appLoop(app, newAppState())
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
//    val config = loadConfig<LabConfig>("labConfig.yaml") ?: LabConfig()
    val gameConfig = loadGameConfig()
//    saveLabConfig(config)
    runApp(createDesktopPlatform("Dev Lab"), gameConfig)
  }
}