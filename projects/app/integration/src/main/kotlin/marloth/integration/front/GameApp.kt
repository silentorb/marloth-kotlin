package marloth.integration.front

import marloth.clienting.*
import marloth.clienting.rendering.getMeshInfo
import marloth.definition.staticDefinitions
import marloth.integration.debug.newDebugHooks
import marloth.integration.misc.*
import persistence.Database
import persistence.newDatabase
import silentorb.mythic.debugging.checkDotEnvChanged
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.editing.closeImGui
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.toPlatformDisplayConfig
import silentorb.mythic.platforming.Platform
import silentorb.mythic.quartz.TimestepState
import silentorb.mythic.quartz.newTimestepState
import silentorb.mythic.scenery.Scene
import simulation.main.World
import simulation.misc.Definitions

typealias RenderHook = (SceneRenderer, Scene) -> Unit
typealias GameUpdateHook = (AppState) -> Unit
typealias TimeStepHook = (TimestepState, Int, AppState) -> Unit
typealias SimpleHook = () -> Unit

data class GameHooks(
    val onRender: RenderHook? = null,
    val onUpdate: GameUpdateHook? = null,
    val onTimeStep: TimeStepHook? = null,
    val onClose: SimpleHook? = null
)

typealias NewWorld = (GameApp) -> World

data class GameApp(
    val platform: Platform,
    val client: Client,
    val db: Database = newDatabase("game.db"),
    val definitions: Definitions,
    val newWorld: NewWorld,
    val hooks: GameHooks? = null
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

fun conditionalDebugHooks(): GameHooks? =
    if (getDebugBoolean("ENABLE_DEBUGGING"))
      newDebugHooks()
    else
      null

fun newGameApp(platform: Platform, client: Client): GameApp {
  val clientDefinitions = definitionsFromClient(client)
  val definitions = staticDefinitions(clientDefinitions, loadApplicationInfo())
  return GameApp(
      platform = platform,
      client = client,
      definitions = definitions,
      newWorld = { gameApp -> generateWorld(definitions, getMeshInfo(gameApp.client)) },
      hooks = conditionalDebugHooks()
  )
}

fun runApp(platform: Platform, options: AppOptions) {
  platform.display.initialize(toPlatformDisplayConfig(options.display))
  val app = newGameApp(platform, newClient(platform, options.display))
  val world = generateWorld(app.definitions, getMeshInfo(app.client))
  val state = AppState(
      client = newClientState(options.input, options.audio, platform.display.getDisplayModes()),
      options = options,
      worlds = listOf(world),
      timestep = newTimestepState()
  )
  gameLoop(app, state)
  println("Closing")
  val onClose = app.hooks?.onClose
  if (onClose != null)
    onClose()

  closeImGui()
  app.client.shutdown()
}
