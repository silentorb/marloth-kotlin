package marloth.integration

import haft.mapEventsToCommands
import marloth.clienting.*
import marloth.clienting.audio.updateAppStateAudio
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.layoutGui
import marloth.clienting.input.GuiCommandType
import marloth.front.GameApp
import marloth.front.RenderHook
import mythic.bloom.next.Box
import mythic.bloom.toAbsoluteBounds
import mythic.ent.pipe
import mythic.platforming.WindowInfo
import mythic.quartz.updateTimestep
import org.joml.Vector2i
import persistence.Database
import persistence.createVictory
import simulation.physics.newBulletState
import simulation.physics.releaseBulletState
import simulation.physics.syncNewBodies
import simulation.*

fun updateSimulationDatabase(db: Database, next: World, previous: World) {
  val nextGameOver = next.gameOver
  if (previous.gameOver == null && nextGameOver != null) {
    if (nextGameOver.winningFaction == 1L)
      createVictory(db, Victory(
          next.players.first().name
      ))
  }
}

fun updateClientFromWorld(worlds: List<World>, client: ClientState): ClientState {
  return if (client.view == ViewId.none && worlds.last().gameOver != null)
    client.copy(
        view = ViewId.victory
    )
  else
    client
}

fun updateWorld(app: GameApp, state: AppState): List<World> {
  val getBinding = getBinding(state.client.input, state.client.input.gameInputProfiles)
  val commands = mapGameCommands(mapEventsToCommands(state.client.input.deviceStates, gameStrokes, getBinding))
  val worlds = state.worlds
  val world = worlds.last()
  val nextWorld = simulation.updateWorld(app.bulletState, app.client.renderer.animationDurations, world, commands, simulationDelta)
  updateSimulationDatabase(app.db, nextWorld, world)
  return worlds
      .plus(nextWorld)
      .takeLast(2)
}

fun restartWorld(app: GameApp, newWorld: () -> World): List<World> {
  releaseBulletState(app.bulletState)

  val world = newWorld()
  app.bulletState = newBulletState()
  syncNewBodies(world, app.bulletState)

  return listOf(world)
}

fun updateFixedInterval(app: GameApp, box: Box, newWorld: () -> World): (AppState) -> AppState = { state ->
  app.platform.process.pollEvents()
  val nextClientState = pipe(state.client, listOf(
      updateClientInput(app.client),
      updateClient(app.client, state.players, box),
      updateAppStateAudio(app.client, state.worlds)
  ))
  val newAppState = state.copy(
      client = nextClientState
  )
  val worlds = when {
    nextClientState.commands.any { it.type == GuiCommandType.newGame } -> restartWorld(app, newWorld)
    gameIsActive(state) -> updateWorld(app, newAppState)
    else -> state.worlds.takeLast(1)
  }

  state.copy(
      client = updateClientFromWorld(worlds, nextClientState),
      worlds = worlds
  )
}

typealias GameUpdateHook = (AppState) -> Unit

data class GameHooks(
    val onRender: RenderHook,
    val onUpdate: GameUpdateHook
)

fun layoutGui(app: GameApp, appState: AppState, windowInfo: WindowInfo): Box {
  val world = appState.worlds.lastOrNull()
  val hudData = if (world != null)
    gatherHudData(world)
  else
    null

  return layoutGui(app.client, appState.client, world, hudData, windowInfo)
}

fun updateAppState(app: GameApp, newWorld: () -> World, hooks: GameHooks? = null): (AppState) -> AppState = { appState ->
  val windowInfo = app.client.getWindowInfo()
  val box = toAbsoluteBounds(Vector2i.zero, layoutGui(app, appState, windowInfo))
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())

  if (steps <= 1) {
    renderMain(app.client, windowInfo, appState, box, hooks?.onRender)
  }

  (1..steps).fold(appState) { state, step ->
    val newBoxes = if (step == 1)
      box
    else
      layoutGui(app, state, windowInfo)

    val result = updateFixedInterval(app, newBoxes, newWorld)(state)
    if (hooks != null) {
      hooks.onUpdate(result)
    }
    result
  }
      .copy(
          timestep = timestep
      )
}
