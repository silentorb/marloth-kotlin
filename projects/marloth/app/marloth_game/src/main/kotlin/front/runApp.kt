package front

import generation.generateDefaultWorld
import haft.ProfileStates
import marloth.clienting.Client
import marloth.clienting.CommandType
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.World
import marloth.clienting.initialGameInputState
import simulation.changing.updateWorld
import visualizing.createScenes

data class App(
    val platform: Platform,
    val config: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val client: Client = Client(platform, config.display, config.input)
)

data class AppState(
    val input: ProfileStates<CommandType> = initialGameInputState(),
    val world: World
)

tailrec fun gameLoop(app: App, state: AppState) {
  app.display.swapBuffers()
  val scenes = createScenes(state.world, app.client.screens)
  val (commands, nextInputState) = app.client.update(scenes, state.input)
  val delta = app.timer.update().toFloat()
  val characterCommands = mapCommands(state.world.players, commands)
  val nextWorld = updateWorld(state.world, characterCommands, delta)
  app.platform.process.pollEvents()

  val nextState = AppState(
      input = nextInputState,
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
      world = world
  )
  gameLoop(app, state)
}
