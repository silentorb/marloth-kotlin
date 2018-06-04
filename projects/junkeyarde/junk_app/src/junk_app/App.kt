package junk_app

import commanding.CommandType
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import junk_client.Client

data class AppState(
    val temp: Int = 0
//    val labInput: InputTriggerState<CommandType>
//    val gameInput: ProfileStates<CommandType>,
//    val menuState: MenuState
)

data class JunkApp(
    val platform: Platform,
    val gameConfig: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val client: Client = Client(platform)
)

tailrec fun appLoop(app: JunkApp, previousState: AppState) {
  app.display.swapBuffers()
//  val scenes = createScenes(app.world, app.client.screens)
  val delta = app.timer.update().toFloat()
//  val (commands, nextState, menuAction) = app.labClient.update(scenes, app.world.meta, previousState, delta)
//  if (app.config.view == Views.game) {
//    val instantiator = Instantiator(app.world, InstantiatorConfig(app.gameConfig.gameplay.defaultPlayerView))
//    val updater = WorldUpdater(app.world, instantiator)
//    updater.update(commands, delta)
//  }
  app.platform.process.pollEvents()

//  if (app.gameConfig.gameplay.defaultPlayerView != app.world.players[0].viewMode) {
//    app.gameConfig.gameplay.defaultPlayerView = app.world.players[0].viewMode
//    saveGameConfig(app.gameConfig)
//  }
//  if (saveIncrement++ > 60 * 3) {
//    saveIncrement = 0
//    saveLabConfig(app.config)
//  }

//  if (menuAction == MenuActionType.newGame) {
//    app.world = app.newWorld()
//  }

  if (!app.platform.process.isClosing())
    appLoop(app, nextState)
}

fun runApp(platform: Platform, gameConfig: GameConfig) {
  platform.display.initialize(gameConfig.display)
  val app = JunkApp(platform, gameConfig)
  appLoop(app, AppState(mapOf(), initialGameInputState(), initialMenuState()))
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