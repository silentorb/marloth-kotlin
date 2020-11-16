package marloth.integration.front

import marloth.clienting.*
import marloth.definition.data.persistence.initialHistoricalData
import marloth.definition.staticDefinitions
import marloth.integration.debug.newDebugHooks
import marloth.integration.misc.*
import persistence.Database
import persistence.newDatabase
import silentorb.mythic.debugging.checkDotEnvChanged
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.toPlatformDisplayConfig
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.quartz.TimestepState
import silentorb.mythic.quartz.newTimestepState
import silentorb.mythic.lookinglass.Scene
import silentorb.mythic.lookinglass.getMeshShapes
import simulation.main.World
import simulation.misc.Definitions

typealias RenderHook = (WindowInfo, AppState) -> Unit
typealias RenderSceneHook = (SceneRenderer, Scene) -> Unit
typealias GameUpdateHook = (AppState) -> Unit
typealias TimeStepHook = (TimestepState, Int, AppState) -> Unit
typealias SimpleHook = () -> Unit

data class GameHooks(
    val onRenderPost: RenderHook? = null,
    val onRenderScene: RenderSceneHook? = null,
    val onUpdate: GameUpdateHook? = null,
    val onTimeStep: TimeStepHook? = null,
    val onClose: SimpleHook? = null
)

typealias NewWorld = (GameApp) -> World

data class GameApp(
    val platform: Platform,
    val client: Client,
    val db: Database,
    val definitions: Definitions,
    val newWorld: NewWorld
)

fun checkSaveOptions(previous: AppOptions, next: AppOptions) {
  if (previous != next) {
    saveGameConfig(next)
  }
}

tailrec fun gameLoop(app: GameApp, state: AppState) {
  if (getDebugBoolean("WATCH_DOT_ENV"))
    checkDotEnvChanged()

  val nextState = updateAppState(app)(state)

  checkSaveOptions(state.options, nextState.options)

  if (!app.platform.process.isClosing())
    gameLoop(app, nextState)
}

fun conditionalHooks(): GameHooks? =
    if (getDebugBoolean("ENABLE_DEBUGGING"))
    newDebugHooks(GameHooks())
  else
    null

fun newGameApp(platform: Platform, client: Client): GameApp {
  val clientDefinitions = definitionsFromClient(client)
  val definitions = staticDefinitions(clientDefinitions, loadApplicationInfo())
  val db = newDatabase("game.db", ::initialHistoricalData)
  return GameApp(
      platform = platform,
      client = client,
      definitions = definitions,
      db = db,
      newWorld = { gameApp -> generateWorld(db, definitions, getMeshShapes(gameApp.client.renderer)) }
  )
}

fun runApp(platform: Platform, options: AppOptions) {
  platform.display.initialize(toPlatformDisplayConfig(options.display))
  val app = newGameApp(platform, newClient(platform, options.display))
  val world = generateWorld(app.db, app.definitions, getMeshShapes(app.client.renderer))
  val state = AppState(
      client = newClientState(options.input, options.audio, platform.display.getDisplayModes()),
      options = options,
      worlds = listOf(world),
      timestep = newTimestepState(),
      hooks = conditionalHooks()
  )
  gameLoop(app, state)
  println("Closing")
  val onClose = state.hooks?.onClose
  if (onClose != null)
    onClose()

  app.client.shutdown()
}
