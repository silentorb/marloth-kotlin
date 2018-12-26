package marloth.integration

import marloth.clienting.CommandType
import marloth.clienting.UserCommands
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

fun updateWorld(db: Database, animationDurations: AnimationDurationMap, state: AppState, userCommands: UserCommands, steps: Int): List<World> {
  val commands = mapCommands(state.players, userCommands)
  val worlds = state.worlds
  return when {
    userCommands.any { it.type == CommandType.newGame } -> worlds

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