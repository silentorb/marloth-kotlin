package marloth.integration

import marloth.clienting.ClientState
import mythic.quartz.TimestepState
import mythic.quartz.updateTimestep
import simulation.main.World
import simulation.updating.simulationDelta

data class AppState(
    val client: ClientState,
    val worlds: List<World>,
    val timestep: TimestepState
)

fun updateAppTimestep(timestepState: TimestepState) = updateTimestep(timestepState, simulationDelta.toDouble())
