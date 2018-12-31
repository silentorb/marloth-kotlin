package marloth.integration

import marloth.clienting.ClientState
import marloth.clienting.gui.gameIsActiveByClient
import marloth.clienting.isGuiActive
import mythic.quartz.TimestepState
import mythic.quartz.updateTimestep
import simulation.World
import simulation.simulationDelta

data class AppState(
    val client: ClientState,
    val players: List<Int>,
    val worlds: List<World>,
    val timestep: TimestepState
)

fun updateAppTimestep(timestepState: TimestepState) = updateTimestep(timestepState, simulationDelta.toDouble())

fun gameIsActive(state: AppState): Boolean = gameIsActiveByClient(state.client)

