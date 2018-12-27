package marloth.integration

import marloth.clienting.ClientState
import marloth.clienting.CommandType
import marloth.clienting.UserCommands
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.currentViewKey
import marloth.clienting.gui.layoutGui
import marloth.clienting.updateClient
import marloth.front.GameApp
import mythic.quartz.updateTimestep
import persistence.Database
import persistence.createVictory
import simulation.AnimationDurationMap
import simulation.Victory
import simulation.World
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

fun updateWorld(db: Database, animationDurations: AnimationDurationMap, state: AppState, userCommands: UserCommands, steps: Int, newWorld: () -> World): List<World> {
  val commands = mapCommands(state.players, userCommands)
  val worlds = state.worlds
  return when {
    userCommands.any { it.type == CommandType.newGame } -> listOf(newWorld())

    worlds.any() && gameIsActive(state) -> {
      (1..steps).fold(worlds) { w, _ ->
        val world = w.last()
        val newWorld = simulation.updateWorld(animationDurations, world, commands, simulationDelta)
        updateSimulationDatabase(db, newWorld, world)
        w.plus(newWorld)
      }
          .takeLast(2)
    }

    else -> worlds
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

fun updateAppState(app: GameApp, newWorld: () -> World): (AppState) -> AppState = { state ->
  app.platform.display.swapBuffers()
  app.platform.process.pollEvents()

  val windowInfo = app.client.getWindowInfo()
  val updatedClient = state.client.copy(
      bloomState = state.client.bloomState.copy(
          bag = state.client.bloomState.bag.plus(currentViewKey to state.client.view)
      )
  )
  val boxes = layoutGui(app.client, updatedClient, state.worlds.lastOrNull(), windowInfo)
  renderMain(app.client, windowInfo, state, boxes)

  val (timestep, steps) = updateTimestep(state.timestep, simulationDelta.toDouble())
  val (nextClientState, commands) = updateClient(app.client, state.players, updatedClient, state.worlds.last(), boxes)
  val worlds = updateWorld(app.db, app.client.renderer.animationDurations, state, commands, steps, newWorld)

  state.copy(
      client = updateClientFromWorld(worlds, nextClientState),
      worlds = worlds,
      timestep = timestep
  )
}