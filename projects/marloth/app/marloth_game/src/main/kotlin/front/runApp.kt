package front

import generation.generateDefaultWorld
import haft.ProfileStates
import marloth.clienting.Client
import marloth.clienting.CommandType
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.WorldMap
import marloth.clienting.initialGameInputState
import simulation.changing.InstantiatorConfig
import simulation.changing.updateWorld
import simulation.createBiomes
import visualizing.createScenes

data class App(
    val platform: Platform,
    val config: GameConfig,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val world: WorldMap,
    val client: Client = Client(platform, config.display, config.input)
)

data class AppState(
    val inputState: ProfileStates<CommandType> = initialGameInputState()
)

tailrec fun gameLoop(app: App, previousState: AppState) {
  app.display.swapBuffers()
  val scenes = createScenes(app.world, app.client.screens)
  val (commands, nextInputState) = app.client.update(scenes, previousState.inputState)
  val delta = app.timer.update().toFloat()
//  val instantiator = Instantiator(app.world, InstantiatorConfig())
  val characterCommands = mapCommands(app.world.players, commands)
  updateWorld(app.world, characterCommands, delta)
  app.platform.process.pollEvents()

  if (!app.platform.process.isClosing())
    gameLoop(app, AppState(nextInputState))
}

//fun runApp(platform: Platform) {
//  val config = loadGameConfig()
//  val biomes = createBiomes()
//  val app = App(
//      platform = platform,
//      config = config,
//      world = generateDefaultWorld(InstantiatorConfig(), biomes)
//  )
//  platform.display.initialize(config.display)
//  setWorldMesh(app.world.meta, app.client)
//  gameLoop(app, AppState())
//}
