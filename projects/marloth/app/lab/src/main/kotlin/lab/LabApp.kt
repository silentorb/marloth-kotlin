package lab

import configuration.loadConfig
import configuration.saveConfig
import front.GameConfig
import front.loadGameConfig
import front.setWorldMesh
import generation.calculateWorldScale
import generation.generateWorld
import generation.placeEnemies
import lab.views.GameViewConfig
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
import simulation.changing.Instantiator
import simulation.changing.InstantiatorConfig
import simulation.changing.WorldUpdater
import visualizing.createScenes
import java.io.File
import kotlin.concurrent.thread
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError


private val watchedPackageFiles = listOf(
    App::class
)
    .map { it.java.getProtectionDomain().getCodeSource().getLocation().toURI() }

val lastTimestamps = watchedPackageFiles.associate { Pair(it, 0L) }.toMutableMap()

fun hasAnyChanged(): Boolean =
    lastTimestamps.any {
      val lastModified = File(it.key).lastModified()
      if (lastModified > it.value) {
        lastTimestamps[it.key] = lastModified
        true
      } else
        false
    }

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

  if (gameViewConfig.haveEnemies) {
    val scale = calculateWorldScale(boundary.dimensions)
    val instantiator = Instantiator(world, instantiatorConfig)
    placeEnemies(world, instantiator, dice, scale)
  }

  return world
}

data class LabApp(
    val platform: Platform,
    val config: LabConfig,
    val gameConfig: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val biomes: List<Biome>,
    var world: World,
    val client: Client = Client(platform, gameConfig.input),
    val labClient: LabClient = LabClient(config, client)
) {

  fun newWorld() =
      generateDefaultWorld(InstantiatorConfig(gameConfig.gameplay.defaultPlayerView), config.gameView, biomes)
}

private var saveIncrement = 0
private var isSaving = false

const val nSecond: Long = 1000000000L
const val maxInterval = 1f / 60f

tailrec fun labLoop(app: LabApp, previousState: LabState) {
  app.display.swapBuffers()
  val delta = app.timer.update().toFloat()
  if (app.timer.actualDelta > maxInterval) {
    val progress = app.timer.last - app.timer.start
    println("" + (progress.toDouble() / nSecond.toDouble()).toFloat() + ": " + app.timer.actualDelta)
  }
//  val nextState = previousState
  val (commands, nextState, menuAction) = app.labClient.update(app.world, app.client.screens, previousState, delta)
  if (app.config.view == Views.game) {
    val instantiator = Instantiator(app.world, InstantiatorConfig(app.gameConfig.gameplay.defaultPlayerView))
    val updater = WorldUpdater(app.world, instantiator)
    updater.update(commands, delta)
  }

  if (menuAction == MenuActionType.newGame) {
    app.world = app.newWorld()
  }

  app.platform.process.pollEvents()

  if (app.gameConfig.gameplay.defaultPlayerView != app.world.players[0].viewMode) {
    app.gameConfig.gameplay.defaultPlayerView = app.world.players[0].viewMode
//    saveGameConfig(app.gameConfig)
  }
  if (saveIncrement++ > 60 * 3 && !isSaving) {
    isSaving = true
    saveIncrement = 0
//    thread {
//    saveLabConfig(app.config)
    isSaving = false
//    }

//    if (hasAnyChanged()) {
//      println("One of the watched packages was modified at " + Date().toString())
//    }
  }

  if (!app.platform.process.isClosing())
    labLoop(app, nextState)
}

fun runApp(platform: Platform, config: LabConfig, gameConfig: GameConfig) {
  platform.display.initialize(gameConfig.display)
  val biomes = createBiomes()
  val world = generateDefaultWorld(InstantiatorConfig(gameConfig.gameplay.defaultPlayerView), config.gameView, biomes)
  val app = LabApp(platform, config, gameConfig, world = world, biomes = biomes)
  setWorldMesh(app.world.meta, app.client)
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
    val config = loadConfig<LabConfig>("labConfig.yaml") ?: LabConfig()
    val gameConfig = loadGameConfig()
    saveLabConfig(config)
//    startGui()
    runApp(createDesktopPlatform("Dev Lab"), config, gameConfig)
  }
}