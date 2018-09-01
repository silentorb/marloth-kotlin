package lab

import configuration.ConfigManager
import configuration.loadConfig
import configuration.saveConfig
import front.GameConfig
import front.loadGameConfig
import front.setWorldMesh
import generation.calculateWorldScale
import generation.generateWorld
import generation.placeEnemies
import lab.views.game.GameViewConfig
import lab.views.game.updateLabWorld
import lab.views.model.newModelViewState
import marloth.clienting.Client
import marloth.clienting.gui.MenuActionType
import marloth.clienting.initialGameInputState
import marloth.clienting.newClientState
import mythic.desktop.createDesktopPlatform
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import randomly.Dice
import simulation.*
import simulation.changing.InstantiatorConfig
import java.io.File
import kotlin.concurrent.thread
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError

const val labConfigPath = "labConfig.yaml"

fun startGui() {
  thread(true, false, null, "JavaFX GUI", -1) {
    //    val gui = LabGui(setModelCode, getModelCode)
    LabGui.main(listOf())
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

fun generateDefaultWorld(instantiatorConfig: InstantiatorConfig, gameViewConfig: GameViewConfig, biomes: List<Biome>): World {
  val boundary = createWorldBoundary(gameViewConfig.worldLength)
  val dice = createDice(gameViewConfig)
  val world = generateWorld(WorldInput(
      boundary,
      dice,
      biomes
  ), instantiatorConfig)

  return if (gameViewConfig.haveEnemies) {
    val scale = calculateWorldScale(boundary.dimensions)
    val nextId = newIdSource(world.nextId)
    val newCharacters = placeEnemies(world.realm, nextId, dice, scale)
    addDeck(world, newCharacters, nextId)
  } else
    world
}

data class LabApp(
    val platform: Platform,
    val config: LabConfig,
    val gameConfig: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val biomes: List<Biome>,
    var world: World,
    val client: Client = Client(platform, gameConfig.display, gameConfig.input),
    val labClient: LabClient = LabClient(config, client),
    val labConfigManager: ConfigManager<LabConfig>
) {

  fun newWorld() =
      generateDefaultWorld(InstantiatorConfig(gameConfig.gameplay.defaultPlayerView), config.gameView, biomes)
}

private var saveIncrement = 0f

tailrec fun labLoop(app: LabApp, previousState: LabState) {
  app.display.swapBuffers()
  val delta = app.timer.update().toFloat()

  val (commands, nextState, menuAction) = app.labClient.update(app.world, app.client.screens, previousState, delta)

  if (menuAction == MenuActionType.newGame) {
    app.world = app.newWorld()
  } else if (app.config.view == Views.game && !nextState.gameClientState.menu.isVisible) {
    app.world = updateLabWorld(app, commands, delta)
  }

  app.platform.process.pollEvents()

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
  platform.display.initialize(gameConfig.display)
  val biomes = createBiomes()
  val world = generateDefaultWorld(InstantiatorConfig(gameConfig.gameplay.defaultPlayerView), config.gameView, biomes)
  val app = LabApp(platform, config, gameConfig, world = world, biomes = biomes, labConfigManager = ConfigManager(labConfigPath, config))
  setWorldMesh(app.world.realm, app.client)
  val clientState = newClientState()
  val state = LabState(
      labInput = mapOf(),
      gameInput = initialGameInputState(),
      modelViewState = newModelViewState(),
      gameClientState = clientState
  )
  labLoop(app, state)
}

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    val a = KotlinReflectionInternalError("Reflection on built-in Kotlin types is not yet fully supported. " +
        "No metadata found for ")
    val s = this.javaClass.`package`.getImplementationVersion()
    val s2 = File(App::class.java.getProtectionDomain().getCodeSource().getLocation().toURI())
    val s3 = s2.lastModified()
    System.setProperty("joml.format", "false")
    val config = loadConfig<LabConfig>(labConfigPath) ?: LabConfig()
    val gameConfig = loadGameConfig()
//    saveLabConfig(config)
//    startGui()
    runApp(createDesktopPlatform("Dev Lab", gameConfig.display), config, gameConfig)
  }
}