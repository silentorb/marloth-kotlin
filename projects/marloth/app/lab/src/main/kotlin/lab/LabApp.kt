package lab

import configuration.ConfigManager
import configuration.loadYamlFile
import configuration.saveYamlFile
import generation.addEnemies
import generation.architecture.MeshInfoMap
import generation.architecture.placeArchitecture
import generation.generateWorld
import lab.utility.updateWatching
import lab.views.game.GameViewConfig
import lab.views.game.drawBulletDebug
import lab.views.model.newModelViewState
import marloth.clienting.Client
import marloth.clienting.newClientState
import marloth.front.GameApp
import marloth.integration.*
import mythic.desktop.createDesktopPlatform
import mythic.ent.pipe
import mythic.quartz.newTimestepState
import simulation.physics.newBulletState
import randomly.Dice
import simulation.main.World
import simulation.misc.WorldInput
import simulation.misc.createWorldBoundary

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

fun generateWorld(meshInfo: MeshInfoMap, gameViewConfig: GameViewConfig): World {
  val boundary = createWorldBoundary(gameViewConfig.worldLength)
  val dice = createDice(gameViewConfig)
  val (initialWorld, graph) = generateWorld(WorldInput(
      boundary,
      dice
  ))

  return pipe(initialWorld, listOf(
      placeArchitecture(meshInfo, graph, dice),
      { world ->
        if (gameViewConfig.haveEnemies)
          addEnemies(world, boundary, dice)
        else
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

  val newWorld = { lab.generateWorld(getMeshInfo(gameApp.client), config.gameView) }
}

private var saveIncrement = 0f

tailrec fun labLoop(app: LabApp, state: LabState) {
  val gameApp = app.gameApp
  val newAppState = if (app.config.view == Views.game) {
    val hooks = GameHooks(
        onRender = if (app.config.gameView.drawPhysics) {
          val deck = state.app.worlds.last().deck
          drawBulletDebug(gameApp, deck.bodies[deck.players.values.first { it.playerId == 1 }.id]!!.position)
        } else
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

fun shutdownGameApp(gameApp: GameApp) {
  gameApp.client.shutdown()
  gameApp.platform.process.shutdownPlatform()
}

fun newLabState(gameApp: GameApp, config: LabConfig): LabState {
  val world = if (config.gameView.autoNewGame)
    lab.generateWorld(getMeshInfo(gameApp.client), config.gameView)
  else
    null

//  if (world != null) {
//    setWorldMesh(world.realm, gameApp.client)
//  }

  val clientState = newClientState(gameApp.platform, gameApp.config.input, gameApp.config.audio)
  return LabState(
      modelViewState = newModelViewState(),
      app = AppState(
          worlds = listOfNotNull(world),
          client = clientState,
          timestep = newTimestepState(),
          players = listOf(1)
      )
  )
}

fun loadLabConfig(): LabConfig =
    loadYamlFile<LabConfig>(labConfigPath) ?: LabConfig()

fun newLabGameApp(labConfig: LabConfig): GameApp {
  val gameConfig = loadGameConfig()
  val platform = createDesktopPlatform("Dev Lab", gameConfig.display)
  platform.display.initialize(gameConfig.display)
  return GameApp(platform, gameConfig,
      bulletState = newBulletState(),
      client = Client(platform, gameConfig.display, labConfig.gameView.lighting)
  )
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    System.setProperty("joml.format", "false")
    println("Starting Lab App")
    val config = loadLabConfig()
    val gameApp = newLabGameApp(config)
    val state = newLabState(gameApp, config)
    val app = LabApp(gameApp, config,
        labConfigManager = ConfigManager(labConfigPath, config),
        labClient = LabClient(config, gameApp.client)
    )
    labLoop(app, state)
    shutdownGameApp(app.gameApp)
  }
}
