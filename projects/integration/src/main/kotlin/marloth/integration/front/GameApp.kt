package marloth.integration.front

import marloth.clienting.*
import marloth.clienting.editing.loadDefaultWorldGraph
import marloth.definition.data.persistence.initialHistoricalData
import marloth.definition.staticDefinitions
import marloth.integration.debug.newDebugHooks
import marloth.integration.misc.*
import persistence.Database
import persistence.newDatabase
import silentorb.mythic.debugging.checkDotEnvChanged
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.lookinglass.Scene
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.toPlatformDisplayConfig
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.quartz.TimestepState
import silentorb.mythic.quartz.newTimestepState
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

data class GameApp(
    val platform: Platform,
    val client: Client,
    val db: Database,
    val definitions: Definitions,
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
  )
}

fun runApp(platform: Platform, options: AppOptions) {
  platform.display.initialize(toPlatformDisplayConfig(options.display))
  val app = newGameApp(platform, newClient(platform, options.display))
  val clientState = newClientState(app.definitions.textLibrary, options.input, options.audio, platform.display.getDisplayModes())
  val worlds = if (clientState.isEditorActive)
    listOf()
  else
    listOf(newWorld(app, loadDefaultWorldGraph()))

  val state = AppState(
      client = clientState,
      options = options,
      worlds = worlds,
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
