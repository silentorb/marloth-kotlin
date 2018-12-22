package marloth.front

import generation.generateDefaultWorld
import marloth.clienting.*
import marloth.clienting.gui.plantGui
import marloth.integration.*
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.newTimestepState
import mythic.quartz.updateTimestep
import simulation.simulationDelta

data class App(
    val platform: Platform,
    val config: GameConfig,
    val display: Display = platform.display,
    val client: Client = Client(platform, config.display)
)

tailrec fun gameLoop(app: App, state: AppState) {
  app.display.swapBuffers()
  app.platform.process.pollEvents()

  val windowInfo = app.client.getWindowInfo()
  val boxes = plantGui(app.client, state.client, state.worlds.lastOrNull(), windowInfo)
  renderMain(app.client, windowInfo, state, boxes)

  val (nextClientState, commands) = updateClient(app.client, state.players, state.client, state.worlds.last(), boxes)
  val (timestep, steps) = updateTimestep(state.timestep, simulationDelta.toDouble())
  val worlds = updateWorld(app.client.renderer.animationDurations, state, commands, steps)

  val nextState = state.copy(
      client = nextClientState,
      worlds = worlds,
      timestep = timestep
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
      worlds = listOf(world),
      timestep = newTimestepState()
  )
  gameLoop(app, state)
}
