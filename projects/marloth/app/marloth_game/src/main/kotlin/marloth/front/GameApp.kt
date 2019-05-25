package marloth.front

import generation.generateDefaultWorld
import marloth.clienting.Client
import marloth.clienting.newClientState
import marloth.integration.AppState
import marloth.integration.GameConfig
import marloth.integration.setWorldMesh
import marloth.integration.updateAppState
import mythic.platforming.Platform
import mythic.quartz.newTimestepState
import persistence.Database
import persistence.newDatabase
import physics.BulletState
import physics.newBulletState
import rendering.SceneRenderer

typealias RenderHook = (SceneRenderer) -> Unit

data class GameApp(
    val platform: Platform,
    val config: GameConfig,
    val client: Client,
    val db: Database = newDatabase("game.db"),
    var bulletState: BulletState
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
    client = Client(platform, config.display)
)

fun runApp(platform: Platform, config: GameConfig) {
  platform.display.initialize(config.display)
  val app = newGameApp(platform, config)
  val world = generateDefaultWorld()
  setWorldMesh(world.realm, app.client)
  val state = AppState(
      client = newClientState(platform, config.input, config.audio),
      players = listOf(1),
      worlds = listOf(world),
      timestep = newTimestepState()
  )
  gameLoop(app, state)
  app.client.shutdown()
}
