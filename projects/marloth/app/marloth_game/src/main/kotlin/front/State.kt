package front

import marloth.clienting.ClientState
import marloth.clienting.CommandType
import marloth.clienting.UserCommands
import mythic.quartz.TimestepState
import mythic.quartz.updateTimestep
import simulation.AnimationDurationMap
import simulation.World
import simulation.simulationDelta

data class AppState(
    val client: ClientState,
    val players: List<Int>,
    val world: World?,
    val timestep: TimestepState
)

fun updateAppTimestep(timestepState: TimestepState) = updateTimestep(timestepState, simulationDelta.toDouble())

fun gameIsActive(state: AppState): Boolean = !state.client.menu.isVisible

fun updateWorld(animationDurations: AnimationDurationMap, state: AppState, userCommands: UserCommands, steps: Int): World? {
  val commands = mapCommands(state.players, userCommands)
  val world = state.world
  return when {
    userCommands.any { it.type == CommandType.newGame } -> world

    world != null && gameIsActive(state) -> {
      (1..steps).fold(world) { w, _ ->
        simulation.updateWorld(animationDurations, w.deck, w, commands, simulationDelta)
      }
    }

    else -> world
  }
}