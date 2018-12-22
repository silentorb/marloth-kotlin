package marloth.integration

import marloth.clienting.CommandType
import marloth.clienting.UserCommands
import simulation.AnimationDurationMap
import simulation.World
import simulation.simulationDelta

fun updateWorld(animationDurations: AnimationDurationMap, state: AppState, userCommands: UserCommands, steps: Int): List<World> {
  val commands = mapCommands(state.players, userCommands)
  val worlds = state.worlds
  return when {
    userCommands.any { it.type == CommandType.newGame } -> worlds

    worlds.any() && gameIsActive(state) -> {
      (1..steps).fold(worlds) { w, _ ->
        val world = w.last()
        w.plus(simulation.updateWorld(animationDurations, world.deck, world, commands, simulationDelta))
      }
          .takeLast(2)
    }

    else -> worlds
  }
}