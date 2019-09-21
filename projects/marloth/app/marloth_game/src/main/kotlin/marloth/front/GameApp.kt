package marloth.front

import marloth.clienting.Client
import marloth.clienting.newClientState
import marloth.definition.staticDefinitions
import marloth.generation.generateDefaultWorld
import marloth.integration.AppState
import marloth.integration.GameConfig
import marloth.integration.newClient
import marloth.integration.updateAppState
import mythic.platforming.Platform
import mythic.quartz.newTimestepState
import persistence.Database
import persistence.newDatabase
import simulation.physics.BulletState
import simulation.physics.newBulletState
import rendering.SceneRenderer
import simulation.misc.Definitions

typealias RenderHook = (SceneRenderer) -> Unit

data class GameApp(
    val platform: Platform,
    val config: GameConfig,
    val client: Client,
    val db: Database = newDatabase("game.db"),
    var bulletState: BulletState,
    val definitions: Definitions
)

tailrec fun gameLoop(app: GameApp, state: AppState) {
  val nextState = updateAppState(app, { throw Error("Not yet implemented.") })(state)

  if (!app.platform.process.isClosing())
    gameLoop(app, nextState)
}

fun newGameApp(platform: Platform, config: GameConfig) = GameApp(
    platform = platform,
    config = config,
    bulletState = newBulletState(),
    client = newClient(platform, config.display),
    definitions = staticDefinitions
)

fun runApp(platform: Platform, config: GameConfig) {
  platform.display.initialize(config.display)
  val app = newGameApp(platform, config)
  val world = generateDefaultWorld()
  val state = AppState(
      client = newClientState(platform, config.input, config.audio),
      players = listOf(1),
      worlds = listOf(world),
      timestep = newTimestepState()
  )
  gameLoop(app, state)
  app.client.shutdown()
}
