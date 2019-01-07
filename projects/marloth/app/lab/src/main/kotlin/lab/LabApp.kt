package lab

import configuration.ConfigManager
import configuration.loadConfig
import configuration.saveConfig
import generation.addEnemies
import generation.generateWorld
import lab.gui.LabGui
import lab.utility.updateWatching
import lab.views.game.GameViewConfig
import lab.views.model.newModelViewState
import marloth.clienting.newClientState
import marloth.front.GameApp
import marloth.integration.*
import mythic.desktop.createDesktopPlatform
import mythic.ent.pipe
import mythic.quartz.globalProfiler
import mythic.quartz.newTimestepState
import mythic.quartz.printProfiler
import randomly.Dice
import simulation.*
import kotlin.concurrent.thread

const val labConfigPath = "labConfig.yaml"

fun saveLabConfig(config: LabConfig) {
  saveConfig("labConfig.yaml", config)
}

var guiClosed = false
var mainAppClosed = false
var gui: LabGui? = null

fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
//    gui = LabGui()
    LabGui.mainMenu(listOf())
    guiClosed = true
  }
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
    val gameApp: GameApp,
    val config: LabConfig,
    val labClient: LabClient,
    val labConfigManager: ConfigManager<LabConfig>
) {

  val newWorld = { generateDefaultWorld(config.gameView) }
}

private var saveIncrement = 0f

tailrec fun labLoop(app: LabApp, state: LabState) {
  val gameApp = app.gameApp
  val newAppState = if (app.config.view == Views.game) {
    val newState = updateAppState(gameApp, app.newWorld)(state.app)
    app.labClient.updateInput(mapOf(), newState.client.input.deviceStates)
    newState
  } else {
    gameApp.platform.display.swapBuffers()
    val (timestep, steps) = updateAppTimestep(state.app.timestep)

    gameApp.platform.process.pollEvents()

    val worlds = state.app.worlds
    val world = state.app.worlds.lastOrNull()

    val (commands, newState) = app.labClient.update(world, gameApp.client.screens, state, timestep.delta.toFloat())

//    val newWorlds = when {
//      commands.any { it.type == GuiCommandType.newGame } -> listOf(app.newWorld())
//      app.config.view == Views.game -> updateWorld(gameApp.db, gameApp.client.renderer.animationDurations, state.app, commands, steps)
//      else -> worlds
//    }

    if (world != null && gameApp.config.gameplay.defaultPlayerView != world.players[0].viewMode) {
      gameApp.config.gameplay.defaultPlayerView = world.players[0].viewMode
    }

    saveIncrement += 1f * timestep.delta.toFloat()
    if (saveIncrement > 1f || app.config.view == Views.game) {
      saveIncrement = 0f
//    saveLabConfig(app.config)
      app.labConfigManager.save()
      updateWatching(app)
    }
    newState.app.copy(
        timestep = timestep
    )
  }

  if (!gameApp.platform.process.isClosing() && !guiClosed)
    labLoop(app, state.copy(
        app = newAppState)
    )
}

fun runApp(gameApp: GameApp, config: LabConfig) {
  globalProfiler().start("world-gen")
  val world = if (config.gameView.autoNewGame)
    generateDefaultWorld(config.gameView)
  else
    null

  globalProfiler().start("app")
  val app = LabApp(gameApp, config,
      labConfigManager = ConfigManager(labConfigPath, config),
      labClient = LabClient(config, gameApp.client)
  )
  if (world != null) {
    setWorldMesh(world.realm, gameApp.client)
  }

  val clientState = newClientState(gameApp.platform, gameApp.config.input)
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
  if (!guiClosed) {
    mainAppClosed = true
  }
  app.gameApp.client.shutdown()
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
    globalProfiler().start("otherStart")
    val platform = createDesktopPlatform("Dev Lab", gameConfig.display)
    platform.display.initialize(gameConfig.display)
    val gameApp = GameApp(platform, gameConfig)
    startGui()
    runApp(gameApp, config)
  }
}