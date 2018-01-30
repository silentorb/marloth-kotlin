package front

import commanding.CommandType
import generation.generateDefaultWorld
import marloth.clienting.Client
import mythic.platforming.Display
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import simulation.World
import simulation.WorldUpdater
import visualizing.createScene
import haft.ProfileStates

data class App(
    val platform: Platform,
    val display: Display = platform.display,
    val timer: DeltaTimer = DeltaTimer(),
    val world: World = generateDefaultWorld(),
    val client: Client = Client(platform)
)

data class AppState(
    val inputState: ProfileStates<CommandType> = mapOf()
)

tailrec fun gameLoop(app: App, previousState: AppState) {
  app.display.swapBuffers()
  val scene = createScene(app.world, app.client.screens[0])
  val (commands, nextInputState) = app.client.update(scene, previousState.inputState)
  val delta = app.timer.update().toFloat()
  val updater = WorldUpdater(app.world)
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
