package lab

import silentorb.mythic.configuration.ConfigManager
import silentorb.mythic.configuration.loadYamlFile
import silentorb.mythic.configuration.saveYamlFile
import generation.architecture.misc.GenerationConfig
import generation.architecture.misc.MeshShapeMap
import generation.architecture.misc.compileArchitectureMeshInfo
import lab.utility.shouldReloadWorld
import lab.utility.updateWatching
import lab.views.model.newModelViewState
import marloth.clienting.newClientState
import generation.architecture.definition.biomeInfoMap
import generation.architecture.definition.meshAttributes
import lab.views.game.*
import marloth.clienting.definitionsFromClient
import marloth.definition.staticDefinitions
import marloth.game.front.GameApp
import marloth.game.front.RenderHook
import marloth.game.integration.*
import silentorb.mythic.desktop.createDesktopPlatform
import silentorb.mythic.ent.pipe
import silentorb.mythic.quartz.newTimestepState
import org.lwjgl.glfw.GLFW
import silentorb.mythic.debugging.getDebugRangeValue
import silentorb.mythic.debugging.setDebugRangeValue
import simulation.main.World
import simulation.misc.Definitions
import simulation.misc.WorldInput
import simulation.misc.createWorldBoundary

const val labConfigPath = "../labConfig.yaml"

fun saveLabConfig(config: LabConfig) {
  Thread {
    saveYamlFile(labConfigPath, config)
  }
}

fun generateWorld(definitions: Definitions, meshInfo: MeshShapeMap, gameViewConfig: GameViewConfig): World {
  val boundary = createWorldBoundary(gameViewConfig.worldLength)
  val generationConfig = GenerationConfig(
      definitions = definitions,
      biomes = biomeInfoMap,
      meshes = compileArchitectureMeshInfo(meshInfo, meshAttributes),
      includeEnemies = gameViewConfig.haveEnemies,
      roomCount = gameViewConfig.roomCount
  )
  val input = WorldInput(
      boundary,
      newGenerationDice()
  )
  return generateWorld(definitions, generationConfig, input)
}

data class LabApp(
    val gameApp: GameApp,
    val config: LabConfig,
    val labClient: LabClient,
    val labConfigManager: ConfigManager<LabConfig>
) {

//  val newWorld = { lab.generateWorld(getMeshInfo(gameApp.client), config.gameView) }
}

private var saveIncrement = 0f

fun labRender(app: LabApp, state: LabState): RenderHook = { sceneRenderer, scene ->
  val world = state.app.worlds.last()
  val deck = world.deck
  val renderer = sceneRenderer.renderer
  if (app.config.gameView.drawPhysics) {
    drawBulletDebug(world.bulletState, deck.bodies[deck.players.keys.first()]!!.position)(sceneRenderer, scene)
  }
  val navMesh = state.app.worlds.last().navMesh
  if (navMesh != null)
    renderNavMesh(renderer, app.config.mapView.display, navMesh)

  conditionalDrawAiTargets(deck, renderer)
  conditionalDrawLights(scene.lights, renderer)
}

fun updateDebugRangeValue(appState: AppState) {
  val events = appState.client.input.deviceStates[0].events
  val increment = 0.01f
  if (events.any { it.index == GLFW.GLFW_KEY_MINUS }) {
    val value = getDebugRangeValue()
    setDebugRangeValue(Math.max(value - increment, 0f))
  } else if (events.any { it.index == GLFW.GLFW_KEY_EQUAL }) {
    val value = getDebugRangeValue()
    setDebugRangeValue(Math.min(value + increment, 1f))
  }
}

tailrec fun labLoop(app: LabApp, state: LabState) {
  val gameApp = app.gameApp
  val newAppState = if (app.config.view == Views.game) {
    val hooks = GameHooks(
        onRender = labRender(app, state),
        onUpdate = { appState ->
          app.labClient.updateInput(mapOf(), appState.client.input.deviceStates)
        }
    )

    val update = updateAppState(gameApp, hooks)
    val fixture = app.config.gameView.fixture
    if (fixture == FixtureId.none)
      update(state.app)
    else
      pipe(update, applyFixture(fixture))(state.app)
  } else {
    gameApp.platform.display.swapBuffers()
    val (timestep, steps) = updateAppTimestep(state.app.timestep)

    gameApp.platform.process.pollEvents()

    val world = state.app.worlds.lastOrNull()

    val (commands, newState) = app.labClient.update(world, state, timestep.delta.toFloat())

//    if (world != null && gameApp.config.gameplay.defaultPlayerView != world.deck.players.values.first().viewMode) {
//      gameApp.config.gameplay.defaultPlayerView = world.deck.players.values.first().viewMode
//    }

    newState.app.copy(
        timestep = timestep,
        worlds = if (shouldReloadWorld) {
          shouldReloadWorld = false
          restartWorld(gameApp, world)
        } else
          state.app.worlds
    )
  }
  updateDebugRangeValue(newAppState)

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
  val world = if (config.gameView.autoNewGame) {
    lab.generateWorld(gameApp.definitions, getMeshInfo(gameApp.client), config.gameView)
  } else
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
          timestep = newTimestepState()
      )
  )
}

fun loadLabConfig(): LabConfig =
    loadYamlFile<LabConfig>(labConfigPath) ?: LabConfig()

fun newLabGameApp(labConfig: LabConfig): GameApp {
  val gameConfig = loadGameConfig()
  val platform = createDesktopPlatform("Dev Lab", gameConfig.display)
  platform.display.initialize(gameConfig.display)
  val client = newClient (platform, gameConfig.display)
  val clientDefinitions = definitionsFromClient(client)
  val definitions = staticDefinitions(clientDefinitions)
  return GameApp(platform, gameConfig,
      client = client,
      definitions = definitions,
      newWorld = { gameApp -> lab.generateWorld(definitions, getMeshInfo(gameApp.client), labConfig.gameView) }
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
