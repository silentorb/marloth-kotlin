package marloth.integration

import haft.mapEventsToCommands
import marloth.clienting.*
import marloth.clienting.audio.updateAppStateAudio
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.currentViewKey
import marloth.clienting.gui.layoutGui
import marloth.front.GameApp
import mythic.bloom.Boxes
import mythic.ent.pipe
import mythic.quartz.updateTimestep
import persistence.Database
import persistence.createVictory
import simulation.Victory
import simulation.World
import simulation.gameStrokes
import simulation.simulationDelta

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
//  return if (worlds.any()) {
//    (1..steps).fold(worlds) { w, _ ->
  val world = worlds.last()
  val newWorld = simulation.updateWorld(app.client.renderer.animationDurations, world, commands, simulationDelta)
  updateSimulationDatabase(app.db, newWorld, world)
  return worlds.plus(newWorld)
//    }
      .takeLast(2)
//  } else worlds
}

fun updateFixedInterval(app: GameApp, boxes: Boxes, newWorld: () -> World): (AppState) -> AppState = { state ->
  app.platform.process.pollEvents()
  val nextClientState = pipe(state.client, listOf(
      updateClientInput(app.client),
      updateClient(app.client, state.players, boxes),
      updateAppStateAudio(app.client, state.worlds)
  ))
  val newAppState = state.copy(
      client = nextClientState
  )
  val worlds = when {
//    commands.any { it.type == GuiCommandType.newGame } -> listOf(newWorld())
    gameIsActive(state) -> updateWorld(app, newAppState)
    else -> state.worlds
  }

  state.copy(
      client = updateClientFromWorld(worlds, nextClientState),
      worlds = worlds
  )
}

fun updateAppState(app: GameApp, newWorld: () -> World): (AppState) -> AppState = { appState ->
  val windowInfo = app.client.getWindowInfo()
  val boxes = layoutGui(app.client, appState.client, appState.worlds.lastOrNull(), windowInfo)
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())

  if (steps <= 1) {
    renderMain(app.client, windowInfo, appState, boxes)
  }

  (1..steps).fold(appState) { state, step ->
    val newBoxes = if (step == 1)
      boxes
    else
      layoutGui(app.client, state.client, state.worlds.lastOrNull(), windowInfo)

    updateFixedInterval(app, newBoxes, newWorld)(state)
  }
      .copy(
          timestep = timestep
      )
}
