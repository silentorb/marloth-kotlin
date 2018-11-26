package lab

import configuration.ConfigManager
import configuration.loadConfig
import configuration.saveConfig
import front.GameConfig
import front.loadGameConfig
import front.setWorldMesh
import generation.addEnemies
import generation.generateWorld
import lab.utility.updateWatching
import lab.views.game.GameViewConfig
import lab.views.game.updateLabWorld
import lab.views.model.newModelViewState
import marloth.clienting.Client
import marloth.clienting.CommandType
import marloth.clienting.newClientState
import mythic.desktop.createDesktopPlatform
import mythic.ent.pipeline
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import mythic.quartz.globalProfiler
import mythic.quartz.printProfiler
import randomly.Dice
import simulation.World
import simulation.WorldInput
import simulation.createWorldBoundary
import simulation.replace
import kotlin.concurrent.thread

const val labConfigPath = "labConfig.yaml"

fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
    //    val gui = LabGui(setModelCode, getModelCode)
//    LabGui.main(listOf())
  }
}

fun saveLabConfig(config: LabConfig) {
  saveConfig("labConfig.yaml", config)
}

fun createDice(config: GameViewConfig) =
    if (config.UseRandomSeed)
      Dice()
    else
      Dice(config.seed)

fun generateDefaultWorld(gameViewConfig: GameViewConfig): World {
  val boundary = createWorldBoundary(gameViewConfig.worldLength)
  val dice = createDice(gameViewConfig)
  val initialWorld = generateWorld(WorldInput(
      boundary,
      dice
  ))

  return pipeline(initialWorld, listOf(
      { world ->
        if (gameViewConfig.haveEnemies)
          addEnemies(world, boundary, dice)
        else
          world
      },
      { world ->
        if (!gameViewConfig.playerGravity) {
          val id = world.players.first().character
          val body = world.deck.bodies.first { it.id == id }
//              .copy(gravity = false)
          world.copy(
              deck = world.deck.copy(
                  bodies = replace(world.deck.bodies, body)
              )
          )
        } else
          world
      }
  ))
}

data class LabApp(
    val platform: Platform,
    val config: LabConfig,
    val gameConfig: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    var world: World,
    val client: Client = Client(platform, gameConfig.display),
    val labClient: LabClient = LabClient(config, client),
    val labConfigManager: ConfigManager<LabConfig>
) {

  fun newWorld() =
      generateDefaultWorld(config.gameView)
}

private var saveIncrement = 0f

tailrec fun labLoop(app: LabApp, previousState: LabState) {
  app.display.swapBuffers()
  val delta = app.timer.update().toFloat()

  app.platform.process.pollEvents()

  val (commands, nextState) = app.labClient.update(app.world, app.client.screens, previousState, delta)

  if (commands.any { it.type == CommandType.newGame }) {
    app.world = app.newWorld()
  } else if (app.config.view == Views.game && !nextState.gameClientState.menu.isVisible) {
    app.world = updateLabWorld(app, commands, delta)
  }

  if (app.gameConfig.gameplay.defaultPlayerView != app.world.players[0].viewMode) {
    app.gameConfig.gameplay.defaultPlayerView = app.world.players[0].viewMode
//    saveGameConfig(app.gameConfig)
  }

  saveIncrement += 1f * delta
  if (saveIncrement > 1f) {
    saveIncrement = 0f
//    saveLabConfig(app.config)
    app.labConfigManager.save()
    updateWatching(app)
  }

  if (!app.platform.process.isClosing())
    labLoop(app, nextState)
}

fun runApp(platform: Platform, config: LabConfig, gameConfig: GameConfig) {
  globalProfiler().start("init-display")
  platform.display.initialize(gameConfig.display)
  globalProfiler().start("world-gen")
  val world = generateDefaultWorld(config.gameView)
  globalProfiler().start("app")
  val app = LabApp(platform, config, gameConfig, world = world, labConfigManager = ConfigManager(labConfigPath, config))
  setWorldMesh(app.world.realm, app.client)
  val clientState = newClientState(gameConfig.input)
  val state = LabState(
      labInput = mapOf(),
      modelViewState = newModelViewState(),
      gameClientState = clientState
  )
  globalProfiler().stop()
  printProfiler(globalProfiler())
  labLoop(app, state)
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    globalProfiler().start("start")
    System.setProperty("joml.format", "false")
    globalProfiler().start("labConfig")
    val config = loadConfig<LabConfig>(labConfigPath) ?: LabConfig()
    globalProfiler().start("gameConfig")
    val gameConfig = loadGameConfig()
//    startGui()
    globalProfiler().start("otherStart")
    runApp(createDesktopPlatform("Dev Lab", gameConfig.display), config, gameConfig)
  }
}