package marloth.front

import marloth.clienting.Client
import marloth.clienting.newClientState
import marloth.definition.staticDefinitions
import marloth.generation.generateWorld
import marloth.integration.*
import mythic.platforming.Platform
import mythic.quartz.newTimestepState
import persistence.Database
import persistence.newDatabase
import randomly.Dice
import simulation.physics.BulletState
import simulation.physics.newBulletState
import rendering.SceneRenderer
import simulation.main.World
import simulation.misc.Definitions

typealias RenderHook = (SceneRenderer) -> Unit

typealias NewWorld = (GameApp) -> World

data class GameApp(
    val platform: Platform,
    val config: GameConfig,
    val client: Client,
    val db: Database = newDatabase("game.db"),
    var bulletState: BulletState,
    val definitions: Definitions,
    val newWorld: NewWorld
)

tailrec fun gameLoop(app: GameApp, state: AppState) {
  val nextState = updateAppState(app)(state)

  if (!app.platform.process.isClosing())
    gameLoop(app, nextState)
}

fun newGameApp(platform: Platform, config: GameConfig) = GameApp(
    platform = platform,
    config = config,
    bulletState = newBulletState(),
    client = newClient(platform, config.display),
    definitions = staticDefinitions,
    newWorld = { gameApp -> generateWorld(getMeshInfo(gameApp.client), Dice()) }
)

fun runApp(platform: Platform, config: GameConfig) {
  platform.display.initialize(config.display)
  val app = newGameApp(platform, config)
  val world = generateWorld(getMeshInfo(app.client))
  val state = AppState(
      client = newClientState(platform, config.input, config.audio),
      worlds = listOf(world),
      timestep = newTimestepState()
  )
  gameLoop(app, state)
  app.client.shutdown()
}
