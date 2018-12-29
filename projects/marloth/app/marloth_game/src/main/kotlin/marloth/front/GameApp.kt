package marloth.front

import generation.generateDefaultWorld
import marloth.clienting.*
import marloth.clienting.gui.layoutGui
import marloth.integration.*
import mythic.platforming.Platform
import mythic.quartz.newTimestepState
import mythic.quartz.updateTimestep
import persistence.Database
import persistence.newDatabase
import simulation.simulationDelta

data class GameApp(
    val platform: Platform,
    val config: GameConfig,
    val client: Client = Client(platform, config.display),
    val db: Database = newDatabase("game.db")
)

tailrec fun gameLoop(app: GameApp, state: AppState) {
  val nextState = updateAppState(app, { throw Error("Not yet implemented.") })(state)

  if (!app.platform.process.isClosing())
    gameLoop(app, nextState)
}

fun runApp(platform: Platform, config: GameConfig) {
  platform.display.initialize(config.display)
  val app = GameApp(
      platform = platform,
      config = config
  )
  val world = generateDefaultWorld()
  setWorldMesh(world.realm, app.client)
  val state = AppState(
      client = newClientState(config.input),
      players = listOf(1),
      worlds = listOf(world),
      timestep = newTimestepState()
  )
  gameLoop(app, state)
  app.client.shutdown()
}
