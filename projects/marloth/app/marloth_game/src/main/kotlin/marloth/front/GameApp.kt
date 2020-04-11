package marloth.front

import marloth.clienting.Client
import marloth.clienting.definitionsFromClient
import marloth.clienting.newClientState
import marloth.definition.staticDefinitions
import marloth.integration.*
import silentorb.mythic.platforming.Platform
import silentorb.mythic.quartz.newTimestepState
import persistence.Database
import persistence.newDatabase
import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.randomly.Dice
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.scenery.LightingConfig
import silentorb.mythic.scenery.Scene
import simulation.main.World
import simulation.misc.Definitions

typealias RenderHook = (SceneRenderer, Scene) -> Unit

typealias NewWorld = (GameApp) -> World

data class GameApp(
    val platform: Platform,
    val config: GameConfig,
    val client: Client,
    val db: Database = newDatabase("game.db"),
    val definitions: Definitions,
    val newWorld: NewWorld
)

tailrec fun gameLoop(app: GameApp, state: AppState) {
  val nextState = updateAppState(app)(state)

  if (!app.platform.process.isClosing())
    gameLoop(app, nextState)
}


fun newGameApp(platform: Platform, config: GameConfig): GameApp {
  val client = newClient(platform, config.display)
  val clientDefinitions = definitionsFromClient(client)
  val definitions = staticDefinitions(clientDefinitions)
  return GameApp(
      platform = platform,
      config = config,
      client = client,
      definitions = definitions,
      newWorld = { gameApp -> generateWorld(definitions, getMeshInfo(gameApp.client), Dice()) }
  )
}

fun runApp(platform: Platform, config: GameConfig) {
  platform.display.initialize(config.display)
  val app = newGameApp(platform, config)
  val world = generateWorld(app.definitions, getMeshInfo(app.client))
  val state = AppState(
      client = newClientState(platform, config.input, config.audio),
      worlds = listOf(world),
      timestep = newTimestepState()
  )
  gameLoop(app, state)
  app.client.shutdown()
}
