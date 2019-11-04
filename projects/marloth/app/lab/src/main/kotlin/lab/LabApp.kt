package lab

import configuration.ConfigManager
import configuration.loadYamlFile
import configuration.saveYamlFile
import generation.abstracted.initializeNodeRadii
import generation.abstracted.newWindingPath
import generation.abstracted.newWindingPathTestStartingWithStair
import generation.architecture.allPolyominoes
import generation.architecture.newBuilders
import generation.generateRealm
import generation.misc.GenerationConfig
import generation.misc.MeshShapeMap
import generation.misc.compileArchitectureMeshInfo
import generation.misc.newRandomizedBiomeGrid
import generation.next.buildArchitecture
import lab.utility.shouldReloadWorld
import lab.utility.updateWatching
import lab.views.game.GameViewConfig
import lab.views.game.drawBulletDebug
import lab.views.model.newModelViewState
import marloth.clienting.newClientState
import marloth.definition.generation.biomeInfoMap
import marloth.definition.generation.meshAttributes
import marloth.definition.staticDefinitions
import marloth.front.GameApp
import marloth.front.RenderHook
import marloth.generation.populateWorld
import marloth.integration.*
import mythic.desktop.createDesktopPlatform
import mythic.ent.newIdSource
import mythic.ent.pipe
import mythic.quartz.newTimestepState
import org.recast4j.detour.NavMeshQuery
import randomly.Dice
import simulation.intellect.navigation.newNavMesh
import simulation.main.Deck
import simulation.main.World
import simulation.main.pipeHandsToDeck
import simulation.misc.WorldInput
import simulation.misc.createWorldBoundary
import simulation.physics.newBulletState

const val labConfigPath = "../labConfig.yaml"

fun saveLabConfig(config: LabConfig) {
  Thread {
    saveYamlFile(labConfigPath, config)
  }
}

fun createDice(config: GameViewConfig) =
    if (config.UseRandomSeed)
      Dice()
    else
      Dice(config.seed)

fun generateWorld(meshInfo: MeshShapeMap, gameViewConfig: GameViewConfig): World {
  val boundary = createWorldBoundary(gameViewConfig.worldLength)
  val dice = createDice(gameViewConfig)
  val generationConfig = GenerationConfig(
      biomes = biomeInfoMap,
      meshes = compileArchitectureMeshInfo(meshInfo, meshAttributes),
      includeEnemies = gameViewConfig.haveEnemies,
      roomCount = gameViewConfig.roomCount
  )
  val input = WorldInput(
      boundary,
      dice
  )
  val grid = if (System.getenv("START_WITH_STAIRS") != null)
    newWindingPathTestStartingWithStair(input.dice, generationConfig.roomCount)
  else
    newWindingPath(input.dice, generationConfig.roomCount)

  val biomeGrid = newRandomizedBiomeGrid(biomeInfoMap, input)
  val realm = generateRealm(generationConfig, input, grid, biomeGrid)
  val nextId = newIdSource(1)
  val deck = pipeHandsToDeck(nextId, listOf(
//      { _ -> placeArchitecture(generationConfig, realm, dice) },
      { _ -> buildArchitecture(generationConfig, dice, grid, realm.cellBiomes, allPolyominoes(), newBuilders()) },
      populateWorld(generationConfig, input, realm)
  ))(Deck())

  val navMesh = if (gameViewConfig.haveEnemies)
    newNavMesh(deck)
  else
    null

  return World(
      deck = deck,
      realm = realm.copy(
          graph = initializeNodeRadii(deck)(realm.graph)
      ),
      nextId = nextId(),
      dice = Dice(),
      availableIds = setOf(),
      logicUpdateCounter = 0,
      navMesh = navMesh,
      navMeshQuery = if (navMesh != null) NavMeshQuery(navMesh) else null
  )
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

fun labRender(app: LabApp, state: LabState): RenderHook = { sceneRenderer ->
  if (app.config.gameView.drawPhysics) {
    val deck = state.app.worlds.last().deck
    drawBulletDebug(app.gameApp, deck.bodies[deck.players.keys.first()]!!.position)(sceneRenderer)
  }
  val navMesh = state.app.worlds.last().navMesh
  if (navMesh != null)
    renderNavMesh(sceneRenderer.renderer, app.config.mapView.display, navMesh)
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

    val update = updateAppState(gameApp, app.newWorld, hooks)
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

    val (commands, newState) = app.labClient.update(world, gameApp.client.screens, state, timestep.delta.toFloat())

    if (world != null && gameApp.config.gameplay.defaultPlayerView != world.deck.players.values.first().viewMode) {
      gameApp.config.gameplay.defaultPlayerView = world.deck.players.values.first().viewMode
    }

    newState.app.copy(
        timestep = timestep,
        worlds = if (shouldReloadWorld) {
          shouldReloadWorld = false
          restartWorld(gameApp, app.newWorld)
        } else
          state.app.worlds
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
      client = newClient(platform, gameConfig.display, labConfig.gameView.lighting),
      definitions = staticDefinitions
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
