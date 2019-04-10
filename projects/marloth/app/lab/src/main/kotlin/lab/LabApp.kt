package lab

import configuration.ConfigManager
import configuration.loadYamlFile
import configuration.saveYamlFile
import generation.addEnemies
import generation.generateWorld
import lab.utility.updateWatching
import lab.views.game.GameViewConfig
import lab.views.game.drawBulletDebug
import lab.views.model.newModelViewState
import marloth.clienting.newClientState
import marloth.front.GameApp
import marloth.integration.*
import mythic.desktop.createDesktopPlatform
import mythic.ent.pipe
import mythic.quartz.globalProfiler
import mythic.quartz.newTimestepState
import mythic.quartz.printProfiler
import physics.newBulletState
import randomly.Dice
import simulation.*

const val labConfigPath = "labConfig.yaml"

fun saveLabConfig(config: LabConfig) {
  Thread {
    saveYamlFile("labConfig.yaml", config)
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
    val hooks = GameHooks(
        onRender = if (app.config.gameView.drawPhysics)
          drawBulletDebug(gameApp)
        else
          { _ -> },
        onUpdate = { appState ->
          app.labClient.updateInput(mapOf(), appState.client.input.deviceStates)
        }
    )

    updateAppState(gameApp, app.newWorld, hooks)(state.app)
  } else {
    gameApp.platform.display.swapBuffers()
    val (timestep, steps) = updateAppTimestep(state.app.timestep)

    gameApp.platform.process.pollEvents()

    val world = state.app.worlds.lastOrNull()

    val (commands, newState) = app.labClient.update(world, gameApp.client.screens, state, timestep.delta.toFloat())

    if (world != null && gameApp.config.gameplay.defaultPlayerView != world.players[0].viewMode) {
      gameApp.config.gameplay.defaultPlayerView = world.players[0].viewMode
    }

    newState.app.copy(
        timestep = timestep
    )
  }

  saveIncrement += 1f * newAppState.timestep.delta.toFloat()
  if (saveIncrement > 5f) {
    saveIncrement = 0f
    saveLabConfig(app.config)
    app.labConfigManager.save()
    updateWatching(app)
  }

  if (!gameApp.platform.process.isClosing())
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

  val clientState = newClientState(gameApp.platform, gameApp.config.input, gameApp.config.audio)
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
  app.gameApp.client.shutdown()
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    globalProfiler().start("start")
    System.setProperty("joml.format", "false")
    globalProfiler().start("labConfig")
    val config = loadYamlFile<LabConfig>(labConfigPath) ?: LabConfig()
    globalProfiler().start("gameConfig")
    val gameConfig = loadGameConfig()
    globalProfiler().start("otherStart")
    val platform = createDesktopPlatform("Dev Lab", gameConfig.display)
    platform.display.initialize(gameConfig.display)
    val gameApp = GameApp(platform, gameConfig,
        bulletState = newBulletState()
    )
//    startGui()
    runApp(gameApp, config)
  }
}