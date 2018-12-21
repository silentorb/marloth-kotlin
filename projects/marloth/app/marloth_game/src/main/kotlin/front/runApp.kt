package front

import generation.generateDefaultWorld
import marloth.clienting.*
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.World
import simulation.updateWorld
import visualizing.createScenes

data class App(
    val platform: Platform,
    val config: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val client: Client = Client(platform, config.display)
)

data class AppState(
    val client: ClientState,
    val players: List<Int>,
    val world: World?
)

tailrec fun gameLoop(app: App, state: AppState) {
  app.display.swapBuffers()
  if (state.world != null) {
    val scenes = createScenes(state.world, app.client.screens)
    renderScenes(app.client, scenes)
//    val players = state.world.players.map { it.playerId }
  }
  app.platform.process.pollEvents()
  val (nextClientState, commands) = updateClient(app.client, state.players, state.client, state.world)
  val delta = app.timer.update().toFloat()
  val nextWorld = if (state.world != null) {
    val characterCommands = mapCommands(state.world.players, commands)
    updateWorld(app.client.renderer.animationDurations, state.world, characterCommands, delta)
  } else
    null

  val nextState = state.copy(
      client = nextClientState,
      world = nextWorld
  )

  if (!app.platform.process.isClosing())
    gameLoop(app, nextState)
}

fun runApp(platform: Platform, config: GameConfig) {
  platform.display.initialize(config.display)
  val app = App(
      platform = platform,
      config = config
  )
  val world = generateDefaultWorld()
  setWorldMesh(world.realm, app.client)
  val state = AppState(
      client = newClientState(config.input),
      players = listOf(1),
      world = world
  )
  gameLoop(app, state)
}
