package main.front

import commanding.CommandType
import generation.generateDefaultWorld
import haft.HaftInputState
import marloth.clienting.Client
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.World
import simulation.changing.WorldUpdater
import marloth.clienting.initialGameInputState
import simulation.changing.Instantiator
import simulation.changing.InstantiatorConfig
import visualizing.createScenes

data class App(
    val platform: Platform,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val world: World = generateDefaultWorld(InstantiatorConfig()),
    val client: Client = Client(platform)
)

data class AppState(
    val inputState: HaftInputState<CommandType> = initialGameInputState()
)

tailrec fun gameLoop(app: App, previousState: AppState) {
  app.display.swapBuffers()
  val scenes = createScenes(app.world, app.client.screens)
  val (commands, nextInputState) = app.client.update(scenes, previousState.inputState)
  val delta = app.timer.update().toFloat()
  val instantiator = Instantiator(app.world, InstantiatorConfig())
  val updater = WorldUpdater(app.world, instantiator)
  updater.update(commands, delta)
  app.platform.process.pollEvents()

  if (!app.platform.process.isClosing())
    gameLoop(app, AppState(nextInputState))
}

fun runApp(platform: Platform) {
  val app = App(platform)
  setWorldMesh(app.world.meta, app.client)
  gameLoop(app, AppState())
}
