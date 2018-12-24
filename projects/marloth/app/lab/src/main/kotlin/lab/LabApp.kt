package lab

import configuration.ConfigManager
import configuration.loadConfig
import configuration.saveConfig
import generation.addEnemies
import generation.generateWorld
import lab.utility.updateWatching
import lab.views.game.GameViewConfig
import lab.views.model.newModelViewState
import marloth.clienting.Client
import marloth.clienting.CommandType
import marloth.clienting.newClientState
import marloth.integration.*
import mythic.desktop.createDesktopPlatform
import mythic.ent.pipe
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.globalProfiler
import mythic.quartz.newTimestepState
import mythic.quartz.printProfiler
import randomly.Dice
import simulation.*
import kotlin.concurrent.thread

const val labConfigPath = "labConfig.yaml"

fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
    //    val gui = LabGui(setModelCode, getModelCode)
//    LabGui.mainMenu(listOf())
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

  return pipe(initialWorld, listOf(
      { world ->
        if (gameViewConfig.haveEnemies)
          addEnemies(world, boundary, dice)
        else
          world
      },
      { world ->
        if (!gameViewConfig.playerGravity) {
          val id = world.players.first().id
          val body = world.deck.bodies.values.first { it.id == id }
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
    val client: Client = Client(platform, gameConfig.display),
    val labClient: LabClient = LabClient(config, client),
    val labConfigManager: ConfigManager<LabConfig>
) {

  fun newWorld() =
      generateDefaultWorld(config.gameView)
}

private var saveIncrement = 0f

tailrec fun labLoop(app: LabApp, state: LabState) {
  app.display.swapBuffers()
  val (timestep, steps) = updateAppTimestep(state.app.timestep)

  app.platform.process.pollEvents()

  val worlds = state.app.worlds
  val world = state.app.worlds.lastOrNull()

  val (commands, nextState) = app.labClient.update(world, app.client.screens, state, timestep.delta.toFloat())

  val newWorlds = when {
    commands.any { it.type == CommandType.newGame } -> listOf(app.newWorld())
    app.config.view == Views.game -> updateWorld(app.client.renderer.animationDurations, state.app, commands, steps)
    else -> worlds
  }

  if (world != null && app.gameConfig.gameplay.defaultPlayerView != world.players[0].viewMode) {
    app.gameConfig.gameplay.defaultPlayerView = world.players[0].viewMode
//    saveGameConfig(app.gameConfig)
  }

  saveIncrement += 1f * timestep.delta.toFloat()
  if (saveIncrement > 1f) {
    saveIncrement = 0f
//    saveLabConfig(app.config)
    app.labConfigManager.save()
    updateWatching(app)
  }

  if (!app.platform.process.isClosing())
    labLoop(app, nextState.copy(
        app = nextState.app.copy(
            worlds = newWorlds,
            timestep = timestep
        )
    ))
}

fun runApp(platform: Platform, config: LabConfig, gameConfig: GameConfig) {
  globalProfiler().start("init-display")
  platform.display.initialize(gameConfig.display)
  globalProfiler().start("world-gen")
  val world = if (config.gameView.autoNewGame)
    generateDefaultWorld(config.gameView)
  else
    null

  globalProfiler().start("app")
  val app = LabApp(platform, config, gameConfig,
      labConfigManager = ConfigManager(labConfigPath, config)
  )
  if (world != null) {
    setWorldMesh(world.realm, app.client)
  }

  val clientState = newClientState(gameConfig.input)
  val state = LabState(
      modelViewState = newModelViewState(),
      app = AppState(
          worlds = listOfNotNull(world),
          client = clientState,
          timestep = newTimestepState(),
          players = listOf(1)
      )
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