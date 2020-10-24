package marloth.integration.front

import marloth.clienting.*
import marloth.clienting.rendering.getMeshInfo
import marloth.definition.staticDefinitions
import marloth.integration.debug.newDebugHooks
import marloth.integration.debug.newEditorHooks
import marloth.integration.misc.*
import persistence.Database
import persistence.newDatabase
import silentorb.mythic.godot.newGodotWrapper
import silentorb.mythic.debugging.checkDotEnvChanged
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getConfigString
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.WindowInfo
import silentorb.mythic.quartz.TimestepState
import silentorb.mythic.scenery.Scene
import simulation.main.World
import simulation.misc.Definitions
import java.lang.management.ManagementFactory

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
    val db: Database = newDatabase("game.db"),
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

fun conditionalHooks(): GameHooks? {
  val debugHooks = if (getDebugBoolean("ENABLE_DEBUGGING"))
    newDebugHooks(GameHooks())
  else
    null

  return if (getDebugBoolean("ENABLE_EDITOR"))
    newEditorHooks(debugHooks ?: GameHooks())
  else
    debugHooks
}

fun newGameApp(platform: Platform, client: Client): GameApp {
  val clientDefinitions = definitionsFromClient(client)
  val definitions = staticDefinitions(clientDefinitions, loadApplicationInfo())
  return GameApp(
      platform = platform,
      client = client,
      definitions = definitions,
      newWorld = { gameApp -> generateWorld(definitions, getMeshInfo(gameApp.client)) }
  )
}

fun runApp(platform: Platform, options: AppOptions) {
  val pid = ManagementFactory.getRuntimeMXBean().name
  val libraryDir = getConfigString("GODOT_LIBRARY_DIR")!!
  val libraryName = getConfigString("GODOT_LIBRARY_NAME")!!
  val godotProjectPath = getConfigString("GODOT_PROJECT_PATH")!!
  val godotWrapper = newGodotWrapper(libraryDir, libraryName)
  val args = arrayOf(
      "--path", godotProjectPath, //--remote-debug 127.0.0.1:6007 --allow_focus_steal_pid 15936 --position 448,240"
  )

  godotWrapper.mythicMain("", args.size, args)

//  platform.display.initialize(toPlatformDisplayConfig(options.display))
//
//  val app = newGameApp(platform, newClient(platform, options.display))
//  val world = generateWorld(app.definitions, getMeshInfo(app.client))
//  val state = AppState(
//      client = newClientState(options.input, options.audio, platform.display.getDisplayModes()),
//      options = options,
//      worlds = listOf(world),
//      timestep = newTimestepState(),
//      hooks = conditionalHooks()
//  )
//  gameLoop(app, state)
//  println("Closing")
//  val onClose = state.hooks?.onClose
//  if (onClose != null)
//    onClose()
//
//  app.client.shutdown()
}
